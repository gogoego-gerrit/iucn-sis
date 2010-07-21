/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.lib.manager.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;

/**
 * PluginLogResource.java
 * 
 * Handle viewing and creating log comments for plugins.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class PluginLogResource extends Resource {
	
	private final String plugin;
	private final String root;

	public PluginLogResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		plugin = (String)request.getAttributes().get("plugin");
		root = ((ManagerApplication)ManagerApplication.getCurrent()).getVMRoot();
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (plugin == null)
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
		for (Node node : nodes) {
			if ("comment".equals(node.getNodeName())) {
				PluginLogWriter.log(root, plugin, node.getTextContent());
				break;
			}
		}
		
		getResponse().setStatus(Status.SUCCESS_OK);
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		if (plugin == null)
			return getListing(variant);
		else {
			File file = new File(root + File.separator + "log" + File.separator + plugin);
			if (!file.exists())
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
						
			try {
				return new InputRepresentation(new FileInputStream(file), variant.getMediaType());
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}
	
	private Representation getListing(Variant variant) throws ResourceException {
		final File folder = new File(root + File.separator + "log");
		final String[] list = folder.list();
		
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("root");
		
		if (list != null) {
			for (String file : list)
				root.appendChild(BaseDocumentUtils.impl.
					createElementWithText(document, "plugin", file));
		}
		
		document.appendChild(root);
		
		return new DomRepresentation(variant.getMediaType(), document);
	}

}
