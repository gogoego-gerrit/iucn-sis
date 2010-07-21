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
package com.solertium.gogoego.server.lib.app.tags.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.gogoego.server.lib.app.tags.resources.TagResource;
import com.solertium.gogoego.server.lib.app.tags.resources.TagSettingsResource;
import com.solertium.gogoego.server.lib.services.ApplicationEvents;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * CollectionTagger.java
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class CollectionTagger extends ApplicationEvents.EventHandler {

	private ExecutionContext ec;
	private String siteID;
	private VFS vfs;

	public CollectionTagger(String siteID, ExecutionContext ec, VFS vfs) {
		super();
		this.siteID = siteID;
		this.ec = ec;
		this.vfs = vfs;
	}

	public void handle(Context context, Request request, Response response) {
		if (!response.getStatus().isSuccess())
			return;

		Document retDocument = null;

		final Document document;
		try {
			document = new DomRepresentation(response.getEntity()).getDocument();
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
			return;
		}

		if (request.getResourceRef().getPath().startsWith("/collections")) {
			String setting = TagSettingsResource.getSetting("tagsInCollectionOrganizer", vfs);
			if ("true".equals(setting))
				retDocument = doHandle(document);
			else
				return;
		} else {
			String setting = TagSettingsResource.getSetting("tagsInFileManager", vfs);
			if ("true".equals(setting)) {
				final VFSPath baseRef;
				try {
					baseRef = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
				} catch (VFSUtils.VFSPathParseException e) {
					TrivialExceptionHandler.ignore(this, e);
					return;
				}
				retDocument = doHandleForFiles(document, baseRef);
			} else
				return;
		}

		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, retDocument));
	}

	private Document doHandleForFiles(Document document, final VFSPath baseRef) {
		final NodeCollection nodes = new NodeCollection(document.getElementsByTagName("response"));

		for (Node node : nodes) {
			String href = null;
			final NodeCollection children = new NodeCollection(node.getChildNodes());
			for (Node curChild : children) {
				if (curChild.getNodeName().equals("href")) {
					try {
						href = URLDecoder.decode(curChild.getTextContent(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						href = curChild.getTextContent();
					}
				}
			}
			final VFSPath path;
			try {
				path = baseRef.child(new VFSPathToken(href));
			} catch (NullPointerException e) {
				continue;
			} catch (IllegalArgumentException e) {
				continue;
			}

			final List<Row> list;
			try {
				list = TagResource.getTagsForURI(path, siteID, ec);
			} catch (DBException e) {
				continue;
			}

			if (list != null && !list.isEmpty())
				for (Row row : list)
					node.appendChild(createElement(document, row));
		}
		return document;
	}

	private Document doHandle(Document document) {
		final NodeCollection nodes = new NodeCollection(document.getElementsByTagName("item"));

		for (Node node : nodes) {
			Element el = (Element) node;
			final List<Row> list;
			try {
				list = TagResource.getTagsForURI(new VFSPath(el.getAttribute("uri")), siteID, ec);
			} catch (IllegalArgumentException e) {
				continue;
			} catch (DBException e) {
				continue;
			}

			if (list != null && !list.isEmpty())
				for (Row row : list)
					el.appendChild(createElement(document, row));
		}

		return document;
	}

	private Element createElement(Document document, Row row) {
		Element element = DocumentUtils.impl.createElementWithText(document, "tag", row.get("name").toString());
		element.setAttribute("id", row.get("id").toString());
		element.setAttribute("attributes", row.get("attributes").toString());
		return element;
	}

}
