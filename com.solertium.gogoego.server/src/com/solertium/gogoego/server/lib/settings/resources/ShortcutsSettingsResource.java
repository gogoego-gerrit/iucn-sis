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
package com.solertium.gogoego.server.lib.settings.resources;

import java.io.IOException;
import java.util.HashSet;

import org.gogoego.api.representations.GoGoEgoInputRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.util.portable.XMLWritingUtils;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ShortcutsSettingsResource.java
 * 
 * Save shortcuts file and reinitialize the shortcuts in 
 * ServerApplication.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ShortcutsSettingsResource extends Resource {
	
	private final VFS vfs;
	private final VFSPath filePath = new VFSPath("/(SYSTEM)/shortcuts.xml");

	public ShortcutsSettingsResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		vfs = ServerApplication.getFromContext(context).getVFS();
				
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		try {
			return new GoGoEgoInputRepresentation(vfs.getInputStream(filePath), variant.getMediaType());
		} catch (IOException e) {
			return new GoGoEgoStringRepresentation("<root></root>");
		}
	}
	
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		Document existing;
		try {
			existing = vfs.getMutableDocument(filePath);
		} catch (NotFoundException e) {
			existing = BaseDocumentUtils.impl.newDocument();
			existing.appendChild(existing.createElement("root"));
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final HashSet<String> mappedShortcuts = new HashSet<String>();
		final NodeCollection nodes = new NodeCollection(existing.getDocumentElement().getChildNodes());
		for (Node node : nodes)
			if (node.getNodeType() != Node.TEXT_NODE)
				mappedShortcuts.add(DocumentUtils.impl.getAttribute(node, "shortcut"));
		
		final StringBuilder failed = new StringBuilder();
		
		final ElementCollection elements = new ElementCollection(document.getDocumentElement().getElementsByTagName("shortcut"));
		for (Element el : elements) {
			final String shortcut = el.getAttribute("shortcut");
			if (mappedShortcuts.contains(shortcut))
				failed.append(XMLWritingUtils.writeTag("p", shortcut));
			else {
				existing.getDocumentElement().appendChild(existing.importNode(el, true));
			}
		}
		
		final String failure = failed.toString();
		if (failure.equals("")) {
			if (DocumentUtils.writeVFSFile(filePath.toString(), vfs, existing)) {
				ServerApplication.getFromContext(getContext()).prepareShortcuts();			
				getResponse().setStatus(Status.SUCCESS_OK);
			} else
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not save");
		}
		else {
			getResponse().setEntity(new StringRepresentation(failed, MediaType.TEXT_HTML));
			getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
		}
		
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}
	
	public void storeRepresentation(Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		if (DocumentUtils.writeVFSFile(filePath.toString(), vfs, document)) {
			ServerApplication.getFromContext(getContext()).prepareShortcuts();			
			getResponse().setStatus(Status.SUCCESS_CREATED);
		} else
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not save");
	}

}
