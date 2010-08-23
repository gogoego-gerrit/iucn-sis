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
package com.solertium.gogoego.server.lib.app.tags.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.gogoego.server.GoGoDebug;

/**
 * SearchRowParser.java
 * 
 * Utility class that parses tag search result rows.
 * 
 * @author carl.scott
 * 
 */
public class SearchRowParser {

	public static ArrayList<String> parseQueryTags(final Document document) {
		return _parseQueryData(document, "tag");
	}

	public static ArrayList<String> parseQueryURIs(final Document document) {
		return _parseQueryData(document, "uri");
	}

	private static ArrayList<String> _parseQueryData(final Document document, final String protocol) {
		ArrayList<String> list = new ArrayList<String>();
		NodeList nodes = document.getElementsByTagName(protocol);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node current = nodes.item(i);
			if (current.getNodeName().equals(protocol))
				list.add(current.getTextContent());
		}
		return list;
	}

	public static HashMap<String, TagData> parse(final Document document) {
		HashMap<String, TagData> uriToTagData = new LinkedHashMap<String, TagData>();
		NodeList nodes = document.getElementsByTagName("row");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("row")) {
				String uri = null, tag = null;
				NodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					Node curChild = children.item(k);
					if (DocumentUtils.impl.getAttribute(curChild, "name").equalsIgnoreCase("uri"))
						uri = curChild.getTextContent();
					else if (DocumentUtils.impl.getAttribute(curChild, "name").equalsIgnoreCase("tag"))
						tag = curChild.getTextContent();
				}

				if (uri != null) {
					GoGoDebug.get("debug").println("Found tag {0} for {1}", tag, uri);
					TagData td = uriToTagData.get(uri);
					if (td == null)
						td = new TagData(uri);
					td.addTag(tag);
					uriToTagData.put(uri, td);
				}
			}
		}
		return uriToTagData;
	}
}
