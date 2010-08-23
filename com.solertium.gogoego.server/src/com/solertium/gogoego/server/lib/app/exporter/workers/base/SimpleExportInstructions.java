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
package com.solertium.gogoego.server.lib.app.exporter.workers.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.data.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.gogoego.server.lib.app.exporter.utils.ExportException;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

/**
 * SimpleExportInstructions.java
 * 
 * @author carl.scott
 * 
 */
public class SimpleExportInstructions {

	public static List<VFSPath> parse(Document document) throws ExportException {
		final List<VFSPath> list = new ArrayList<VFSPath>();
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
		for (Node node : nodes) {
			if (node.getNodeName().equals("file")) {
				try {
					list.add(VFSUtils.parseVFSPath(node.getTextContent()));
				} catch (VFSUtils.VFSPathParseException e) {
					throw new ExportException(Status.CLIENT_ERROR_BAD_REQUEST, e);
				}
			}
		}
		return list;
	}

	public static Map<VFSPath, List<String>> parseTagDocument(Document document) throws ExportException {
		final Map<VFSPath, List<String>> map = new HashMap<VFSPath, List<String>>();

		final ElementCollection files = new ElementCollection(document.getDocumentElement()
				.getElementsByTagName("file"));

		for (Element file : files) {

			NodeList urls = file.getElementsByTagName("url");
			if (urls.getLength() != 1)
				throw new ExportException();

			VFSPath path;

			try {
				path = VFSUtils.parseVFSPath(urls.item(0).getTextContent());
			} catch (VFSUtils.VFSPathParseException e) {
				throw new ExportException(Status.CLIENT_ERROR_BAD_REQUEST, e);
			}

			List<String> tagList = new ArrayList<String>();
			final ElementCollection tags = new ElementCollection(file.getElementsByTagName("tag"));
			for (Element tag : tags) {
				tagList.add(tag.getTextContent());
			}

			map.put(path, tagList);

		}

		return map;
	}

	public static Document createTagDocument(Map<VFSPath, List<String>> tags) {

		StringBuilder string = new StringBuilder("<tags>\r\n");
		for (Entry<VFSPath, List<String>> entry : tags.entrySet()) {
			List<String> tagList = entry.getValue();
			if (tagList != null && tagList.size() > 0) {
				string.append("<file>");
				string.append("<url>" + entry.getKey().toString() + "</url>\r\n");

				for (String tag : tagList) {
					string.append("<tag>" + tag + "</tag>\r\n");
				}
				string.append("</file>\r\n");
			}
		}

		string.append("</tags>");
		return DocumentUtils.impl.createDocumentFromString(string.toString());

	}

}
