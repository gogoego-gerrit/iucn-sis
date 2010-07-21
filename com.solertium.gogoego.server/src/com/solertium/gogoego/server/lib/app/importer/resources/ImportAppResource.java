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
package com.solertium.gogoego.server.lib.app.importer.resources;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.importer.container.ImporterApplication;
import com.solertium.gogoego.server.lib.app.importer.container.ImporterSettings;
import com.solertium.gogoego.server.lib.app.importer.worker.ImportMode;
import com.solertium.gogoego.server.lib.app.importer.worker.ZipImporter;
import com.solertium.util.MD5Hash;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ImportAppResource.java
 * 
 * @author carl.scott
 * 
 */
public class ImportAppResource extends Resource {
	
	private final VFS vfs;
	private final ImportMode mode;
	private final String key;
	
	private final ImporterSettings settings;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public ImportAppResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		this.vfs = ServerApplication.getFromContext(context).getVFS();
		String mode = request.getResourceRef().getQueryAsForm().getFirstValue("mode");
		if (!ImportMode.OVERWRITE.toString().equals(mode))
			this.mode = ImportMode.FRESHEN;
		else
			this.mode = ImportMode.OVERWRITE;
		//this.mode = mode;
		this.key = request.getResourceRef().getQueryAsForm().getFirstValue("key");
		
		this.settings = ((ImporterApplication)ServerApplication.
				getFromContext(context, ImporterApplication.REGISTRATION)).getSettings();
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}
	
	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final FileItemFactory factory = new DiskFileItemFactory();
		final RestletFileUpload upload = new RestletFileUpload(factory);
		
		final List<FileItem> items;
		try {
			items = upload.parseRequest(getRequest());
		} catch (FileUploadException e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		FileItem zipFile = null, overwritePreference = null;
		for (final FileItem item : items) {
			if (item.isFormField())
				overwritePreference = item;
			else
				zipFile = item;
		}
		
		if (zipFile == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		final ImportMode mode;
		if (overwritePreference != null && ("true".equals(overwritePreference.getString()) || "on".equals(overwritePreference.getString())))
			mode = ImportMode.OVERWRITE;
		else
			mode = ImportMode.FRESHEN;
		
		final ZipInputStream zis;
		try {
			zis = new ZipInputStream(zipFile.getInputStream());
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final ZipImporter importer = new ZipImporter(vfs);
		importer.restrict(new VFSPath("/(SYSTEM)"));
		
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new GoGoEgoDomRepresentation(importer.doImport(zis, mode)));
	}

	/**
	 * Add new data
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		verifyKey();
		
		final ZipInputStream zis;
		try {
			zis = new ZipInputStream(entity.getStream());
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final ZipImporter importer = new ZipImporter(vfs);
		
		final Document document = importer.doImport(zis, mode);
		
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new GoGoEgoDomRepresentation(document));
	}

	private void verifyKey() throws ResourceException {
		final String myKey = 
			new MD5Hash(settings.getSetting("key", "changeme")).toString();
		
		if (!myKey.equals(key))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Please provide a valid import key.");
	}
	
}
