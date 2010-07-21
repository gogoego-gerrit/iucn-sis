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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.collections.CategoryData;
import org.gogoego.api.collections.CollectionResourceBuilder;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.collections.objects.CollectionFileUpdating;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;
import com.solertium.util.MD5Hash;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.util.restlet.RestletUtils;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSException;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.events.VFSDeleteEvent;
import com.solertium.vfs.events.VFSMoveEvent;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * AdminToolResource.java
 * 
 * @author carl.scott
 * 
 */
public class AdminToolResource extends Resource {

	private final VFSPath uri;
	private final VFS vfs;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public AdminToolResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		VFSPath uri;
		try {
			uri = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
			//GoGoDebug.get("debug").println("AdminToolResource: Working URI is " + uri);
		} catch (Exception e) {
			//GoGoDebug.get("debug").println("AdminToolResource: URI is null");
			e.printStackTrace();
			uri = null;
		}

		this.uri = uri;
		this.vfs = ServerApplication.getFromContext(context).getVFS();

		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
	
	private GoGoDebugger debug() {
		return GoGoDebug.get("debug");
	}

	public Representation represent(Variant variant) throws ResourceException {
		if (uri == null) {
			debug().println("Bad url {0} given.", getRequest().getResourceRef().getRemainingPart());
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}

		if (!vfs.exists(uri)) {
			debug().println("Requested resource {0} not found", uri);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}

		try {
			if (vfs.isCollection(uri))
				throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		} catch (NotFoundException impossible) {
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}

		Representation rep = null;
		if (uri.equals(TemplateRegistry.REGISTRY_PATH)) {
			final TemplateRegistry registry = ServerApplication.getFromContext(getContext()).getTemplateRegistry();
			registry.refresh();

			getResponse().setStatus(Status.SUCCESS_OK);
			rep = new DomRepresentation(MediaType.TEXT_XML, registry.getDocument());
		} else {
			final Request req = new InternalRequest(getRequest(), Method.GET, "riap://host/admin/files"
					+ uri.toString(), null);
			req.getAttributes().putAll(getRequest().getAttributes());
			req.getCookies().addAll(getRequest().getCookies());

			final Response resp = getContext().getClientDispatcher().handle(req);

			getResponse().setStatus(resp.getStatus());
			rep = resp.getEntity();

			if (!resp.getStatus().isSuccess()) {
				debug().println("DAV failure: {0}", resp.getStatus());
				return rep;
			}
		}

		RestletUtils.addHeaders(getResponse(), "content-disposition", "attachment; filename=" + uri.getName());

		return rep;
	}

	public void handlePost() {
		try {
			if (uri == null) {
				debug().println("Bad url {0} given.", getRequest().getResourceRef().getRemainingPart());
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			} else {
				String action = getRequest().getResourceRef().getQueryAsForm().getFirstValue("action");
				if (action != null && action.equals("upload"))
					storeRepresentation(getRequest().getEntity());
				else
					acceptRepresentation(getRequest().getEntity());
			}
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			return;
		}

		String operation = RestletUtils.getHeader(getRequest(), "Operation");

		if (operation.equals("password")) {
			setPassword(document);
		} else if (operation.equals("move") || operation.equals("copy") || operation.equals("rename")) {
			VFSPath to = null;
			ArrayList<VFSPath> fromList = new ArrayList<VFSPath>();

			final NodeList nodes = document.getFirstChild().getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				final Node current = nodes.item(i);
				if (current.getNodeName().equals("from"))
					try {
						fromList.add(VFSUtils.parseVFSPath(current.getTextContent()));
					} catch (VFSUtils.VFSPathParseException e) {
						throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
					}
				else if (current.getNodeName().equals("to"))
					try {
						to = VFSUtils.parseVFSPath(current.getTextContent());
					} catch (VFSUtils.VFSPathParseException e) {
						throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
					}
			}

			if (to == null || fromList.isEmpty())
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

			final Iterator<VFSPath> iterator = fromList.listIterator();
			while (iterator.hasNext()) {
				final VFSPath currentFrom = iterator.next();

				VFSPathToken fileName;
				VFSPath currentTo;

				if (operation.equals("rename")) {
					currentTo = to;
					to = uri.getCollection();
					fileName = new VFSPathToken(currentTo.getName());
					operation = "move";
				} else {
					fileName = new VFSPathToken(currentFrom.getName());
					currentTo = to.child(fileName);
				}

				debug().println("Moving {0} to {1}", currentFrom, currentTo);

				try {
					final VFSPathToken[] listing = vfs.list(to);
					boolean willDuplicate = false;
					for (int k = 0; k < listing.length && !willDuplicate; k++)
						willDuplicate = fileName.equals(listing[k]);
					if (willDuplicate)
						currentTo = to.child(getCopyOfFilename(fileName, listing));
				} catch (Exception e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
				}

				final Request req = getOperationRequest(operation, currentFrom, currentTo);
				debug().println("Destination: {0}", RestletUtils.getHeader(req, "Destination"));

				final Response resp = getContext().getClientDispatcher().handle(req);
				getResponse().setStatus(resp.getStatus());
				if (resp.getStatus().isSuccess()) {
					debug().println("{0} was successful.", operation.toUpperCase());
					if (!"copy".equals(operation))
						new Thread(new CollectionFileUpdating(vfs, new VFSMoveEvent(currentFrom, currentTo), getContext())).start();
				}
				else
					debug().println("{0} failed: {1}", operation.toUpperCase(), resp.getStatus());
			}
		} else if (operation.equals("new")) {
			VFSPathToken name = null;
			boolean isCollection = true;

			final NodeList nodes = document.getFirstChild().getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				final Node current = nodes.item(i);
				if (current.getNodeName().equals("folder") || current.getNodeName().equals("file")) {
					try {
						name = new VFSPathToken(current.getTextContent());
						isCollection = current.getNodeName().equals("folder");
					} catch (IllegalArgumentException e) {
						throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
					}
				}
			}

			if (name == null)
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

			if (isCollection) {
				try {
					vfs.makeCollection(uri.child(name));
				} catch (Exception e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
				}
				debug().println("Created folder {0}", name);
			} else {
				final VFSPath filePath = uri.child(name);
				if (vfs.exists(filePath))
					throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
				
				if (name.toString().endsWith(".html")) {
					
					String css = null, template = null;
					if (vfs.exists(new VFSPath("/css/styles.css")))
						css = "<link rel=\"stylesheet\" type=\"text/css\" src=\"/css/styles.css\" />";
					else if (vfs.exists(new VFSPath("/css/style.css")))
						css = "<link rel=\"stylesheet\" type=\"text/css\" src=\"/css/style.css\" />";
	
					final TemplateRegistry registry = ServerApplication.getFromContext(getContext()).getTemplateRegistry();
					if (registry.isRegistered("default.html")) {
						TemplateDataAPI data = registry.getRegisteredTemplate("default.html");
						if (data.isAllowed(uri.child(name).toString()))
							template = "<meta name=\"template\" content=\"" + data.getUri() + "\" />";
					}
	
					final Document html = DocumentUtils.impl
							.createDocumentFromString("<html>\r\n<head>\r\n<title>New Page</title>\r\n"
									+ (template == null ? "" : (template + "\r\n")) + (css == null ? "" : (css + "\r\n"))
									+ "</head>\r\n<body>\r\n<p></p>\r\n</body>\r\n</html>");

					getResponse().setStatus(
							DocumentUtils.writeVFSFile(filePath.toString(), vfs, html) ? Status.SUCCESS_CREATED
									: Status.SERVER_ERROR_INTERNAL);
				}
				else {
					try {
						final BufferedWriter writer = new BufferedWriter(vfs.getWriter(filePath));
						writer.write("");
						writer.close();
						getResponse().setStatus(Status.SUCCESS_CREATED);
					} catch (final IOException exception) {
						exception.printStackTrace();
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, exception);
					}
				}
			}
		}
	}

	private Request getOperationRequest(final String operation, final VFSPath currentFrom, final VFSPath currentTo) {
		final Request req = new InternalRequest(getRequest(), new Method(operation), "riap://host/admin/files"
				+ currentFrom.toString(), null);
		req.getCookies().addAll(getRequest().getCookies());
		RestletUtils.addHeaders(req, "Destination", currentTo.toString());
		return req;
	}

	/**
	 * Sets a password for a resource
	 * 
	 * @param doc
	 *            the instruction document
	 */
	private void setPassword(final Document doc) {
		String uri = null, pw = null, command = null;
		NodeList nodes = doc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node cur = nodes.item(i);
			if (cur.getNodeName().equals("uri"))
				uri = cur.getTextContent();
			else if (cur.getNodeName().equals("password"))
				pw = cur.getTextContent();
			else if (cur.getNodeName().equals("command"))
				command = cur.getTextContent();
		}

		if (command == null || uri == null)
			return;

		VFSPath uriPath;
		try {
			uriPath = VFSUtils.parseVFSPath(uri);
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
			return;
		}

		if (command.indexOf("collection") != -1) {
			CollectionResourceBuilder b = new CollectionResourceBuilder(getContext());
			CategoryData categoryData = b.getCurrentCategory(uriPath);

			if (categoryData.getItemID() == null)
				uri = "/(SYSTEM)/" + uri;
			else
				uri = "/(SYSTEM)/" + uri + ".xml";
		}

		uriPath = new VFSPath(uri);

		VFSMetadata md = vfs.getMetadata(uriPath);
		if (command.startsWith("delete")) {
			md.getSecurityProperties().remove(VFSMetadata.PASSWORD_PROTECTED);
		} else if (command.startsWith("create") && pw != null) {
			md.addSecurityProperty(VFSMetadata.PASSWORD_PROTECTED, new MD5Hash(pw).toString());
		}

		try {
			vfs.setMetadata(uriPath, md);
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	/**
	 * Creates a windows-like copy of a file, checking if a file exists and
	 * attempting to create a unique filename
	 * 
	 * Technically there is the possibility of an infinite loop, but it's highly
	 * unlikely.
	 * 
	 * @param fileName
	 *            the original filename
	 * @param listing
	 *            a list of all files in a folder
	 * @return the unique name
	 */
	private VFSPathToken getCopyOfFilename(VFSPathToken fileName, VFSPathToken[] listing) {
		int index = fileName.toString().indexOf(".");
		ArrayList<String> curFiles = new ArrayList<String>();
		for (int i = 0; i < listing.length; i++)
			curFiles.add(listing[i].toString());

		if (index != -1) {
			String start = fileName.toString().substring(0, index);
			String end = fileName.toString().substring(index);
			int copyCount = 0;

			while (curFiles.contains(start + "[" + copyCount + "]" + end))
				copyCount++;

			return new VFSPathToken(start + "[" + copyCount + "]" + end);
		} else {
			int copyCount = 0;
			while (curFiles.contains(fileName + "[" + copyCount + "]"))
				copyCount++;

			return new VFSPathToken(fileName + "[" + copyCount + "]");
		}
	}

	public void handleDelete() {
		if (uri == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else if (uri.equals(VFSPath.ROOT)) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else
			try {
				removeRepresentations();
			} catch (ResourceException e) {
				getResponse().setStatus(e.getStatus());
			}
	}

	public void removeRepresentations() throws ResourceException {
		if (!vfs.exists(uri))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

		final Request req = new InternalRequest(getRequest(), Method.DELETE, "riap://host/admin/files" + uri, null);
		final Response resp = getContext().getClientDispatcher().handle(req);
		getResponse().setStatus(resp.getStatus());

		if (!resp.getStatus().isSuccess())
			debug().println("Delete failed for " + uri);
		else
			new Thread(new CollectionFileUpdating(vfs, new VFSDeleteEvent(uri), getContext())).start();
	}

	public void handlePut() {
		getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}

	public void storeRepresentation(Representation entity) throws ResourceException {
		Document doc = DocumentUtils.impl.newDocument();

		final Element root = doc.createElement("root");
		doc.appendChild(root);

		final FileItemFactory factory = new DiskFileItemFactory();
		final RestletFileUpload uploadServlet = new RestletFileUpload(factory);

		final List<FileItem> items;
		try {
			items = uploadServlet.parseRequest(getRequest());
		} catch (FileUploadException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		for (final FileItem item : items)
			if (!item.isFormField()) {
				if (item.getFieldName().equals("")) {
					Element failure = doc.createElement("file");
					failure.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "name", uri.getName()));
					failure.appendChild(DocumentUtils.impl.createElementWithText(doc, "status", "FAILURE"));
					failure.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "notes",
							"No field name supplied"));

					root.appendChild(failure);
					continue;
				}

				final OutputStream out;
				try {
					out = vfs.getOutputStream(uri);
					out.write(item.get());
					out.close();
				} catch (VFSException e) {
					continue;
				} catch (IOException e) {
					continue;
				}

				Element success = doc.createElement("file");
				success.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "name", uri.getName()));
				success.appendChild(DocumentUtils.impl.createElementWithText(doc, "status", "SUCCESS"));
				root.appendChild(success);
			}

		String out = DocumentUtils.impl.serializeDocumentToString(doc, true, true);
		/*debug().println(out);*/

		getResponse().setStatus(Status.SUCCESS_MULTI_STATUS);
		getResponse().setEntity(new StringRepresentation(out, MediaType.TEXT_HTML));
	}
}
