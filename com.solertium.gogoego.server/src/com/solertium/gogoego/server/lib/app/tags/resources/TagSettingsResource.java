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
package com.solertium.gogoego.server.lib.app.tags.resources;

import java.io.IOException;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * TagSettingsResource.java
 * 
 * Handles settings for tags. These are just kept on the file system at -
 * /(SYSTEM)/tags/settings.xml
 * 
 * Document should look like:
 * 
 * <root> <setting name="...">{value}</setting> </root>
 * 
 * @author carl.scott
 * 
 */
public class TagSettingsResource extends Resource {

	private static final VFSPath SETTINGS_DOC = new VFSPath("/(SYSTEM)/tags/settings.xml");

	private final VFS vfs;

	public TagSettingsResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		this.vfs = ServerApplication.getFromContext(context).getVFS();

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public static Document getSettingsDoc(final VFS vfs) {
		try {
			return vfs.getDocument(SETTINGS_DOC);
		} catch (IOException e) {
			final Document document = DocumentUtils.impl.newDocument();
			document.appendChild(document.createElement("root"));
			return document;
		}
	}

	public static String getSetting(final String key, final VFS vfs) {
		final Document document = getSettingsDoc(vfs);

		String value = null;
		final NodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength() && value == null; i++) {
			final Node node = nodes.item(i);
			if (node.getNodeName().equals("setting") && DocumentUtils.impl.getAttribute(node, "name").equals(key))
				value = node.getTextContent();
		}
		return value;
	}

	public Representation represent(final Variant variant) throws ResourceException {
		return new DomRepresentation(variant.getMediaType(), getSettingsDoc(vfs));
	}

	public void storeRepresentation(final Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}

		DocumentUtils.writeVFSFile(SETTINGS_DOC.toString(), vfs, document);
	}

}
