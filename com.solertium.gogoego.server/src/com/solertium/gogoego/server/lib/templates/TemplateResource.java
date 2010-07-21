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
package com.solertium.gogoego.server.lib.templates;

import java.util.HashMap;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.vfs.restlet.VFSProvidingApplication;

/**
 * TemplateResource.java
 * 
 * Does admin-related operations on HTML files considered to be templates. The
 * template must start with an HTML tag in order to have the certain operations
 * performed on it.
 * 
 * @author carl.scott
 * 
 */
public class TemplateResource extends Resource {

	private final String uri;
	private final TemplateRegistry registry;

	public TemplateResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		uri = request.getResourceRef().getRemainingPart();
		registry = TemplateRegistry.getFromContext(context);
	}

	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (!registry.isRegistered(uri))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		

		Document doc;
		try {
			doc = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}

		String method = doc.getDocumentElement().getAttribute("method");
		if (method.equals("setAttributes")) {
			updateTemplateAttributes(doc);
		}
	}

	private void updateTemplateAttributes(Document doc) throws ResourceException {
		HashMap<String, String> attributes = new HashMap<String, String>();
		NodeList nodes = doc.getElementsByTagName("attribute");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node current = nodes.item(i);
			if (current.getNodeName().equals("attribute")) {
				attributes.put(DocumentUtils.impl.getAttribute(current, "name"), DocumentUtils.impl.getAttribute(
						current, "content"));
			}
		}
		
		if (attributes.isEmpty())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try {
			boolean success = ((TemplateData)registry.getRegisteredTemplate(uri))
				.updateTemplateAttributes(((VFSProvidingApplication) Application.getCurrent()).getVFS(),
					attributes);
			if (success) {
				registry.save();
				getResponse().setStatus(Status.SUCCESS_OK);
				getResponse().setEntity(getStatusDocumentAsEntity("Success"));
			}
			else
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		} catch (IndexOutOfBoundsException e) {
			getResponse().setStatus(Status.SUCCESS_OK);
			getResponse().setEntity(getStatusDocumentAsEntity("Error: Head element not found"));
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	private Representation getStatusDocumentAsEntity(String text) {
		return new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createConfirmDocument(text));
	}

}
