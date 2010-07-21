/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.gogoego.server.lib.clienttools;

import java.io.IOException;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.restlet.VFSResource;

/**
 * UploadRestlet.java
 * 
 * Restlet that handles file uploading
 * 
 * @author carl.scott
 * 
 */
public class UploadRestlet extends Restlet {

	private final VFS vfs;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the context
	 */
	public UploadRestlet(final Context context) {
		super(context);
		vfs = ServerApplication.getFromContext(context).getVFS();
	}

	/**
	 * This method only handles POSTs, anything else is not allowed.
	 */
	public void handle(final Request request, final Response response) {
		if (request.getMethod().equals(Method.POST))
			try {
				handlePost(request, response);
				response.setStatus(Status.SUCCESS_OK);
			} catch (final Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		else
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}

	/**
	 * Handles a form with files to be uploaded, and simultaneously builds a
	 * result document for the given files. Note that this method will always
	 * return success, you'll need to interrogate the document returned to get
	 * more specific results.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the resonse
	 * @throws IOException
	 */
	private void handlePost(final Request request, final Response response) throws IOException, FileUploadException {
		
		Document doc = DocumentUtils.impl.newDocument();

		final Element root = doc.createElement("root");
		doc.appendChild(root);

		final FileItemFactory factory = new DiskFileItemFactory();
		final RestletFileUpload uploadServlet = new RestletFileUpload(factory);

		final VFSPath uri;
		try {
			uri = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}

		if (!vfs.exists(uri))
			vfs.makeCollections(uri);

		if (!vfs.isCollection(uri)) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}

		final List<FileItem> items = uploadServlet.parseRequest(request);
		for (final FileItem item : items)
			if (!item.isFormField()) {
				final VFSPathToken filename = UploadWorker.cleanFilename(item.getName());

				if (!UploadWorker.upload(item, uri, vfs)){
					Element failure = doc.createElement("file");
					failure
							.appendChild(DocumentUtils.impl
									.createCDATAElementWithText(doc, "name", filename.toString()));
					failure.appendChild(DocumentUtils.impl.createElementWithText(doc, "status", "FAILURE"));
					failure.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "notes",
							"No field name supplied"));

					root.appendChild(failure);
					continue;
				}

				Element success = doc.createElement("file");
				success.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "name", filename.toString()));
				success.appendChild(DocumentUtils.impl.createElementWithText(doc, "status", "SUCCESS"));
				root.appendChild(success);
			}

		response.setStatus(Status.SUCCESS_MULTI_STATUS);
		response.setEntity(new StringRepresentation(DocumentUtils.impl.serializeDocumentToString(doc, true),
				MediaType.TEXT_HTML));
	}

}
