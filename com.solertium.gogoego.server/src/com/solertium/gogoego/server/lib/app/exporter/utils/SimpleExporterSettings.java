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
package com.solertium.gogoego.server.lib.app.exporter.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.util.NodeCollection;

/**
 * SimpleExporterSettings.java
 * 
 * Read the settings for an ExporterWorker.  Has ability to load 
 * documents and fail when required settings are missing, and 
 * can write to compatible XML, and exclude fields if necessary.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class SimpleExporterSettings extends ConcurrentHashMap<String, String> {
	
	private static final long serialVersionUID = 1L;
	
	public boolean setProperty(String key, String value) {
		if (value == null)
			return false;
		
		put(key, value);
		return true;
	}
	
	public boolean loadConfig(Document document) {
		return loadConfig(document, new ArrayList<String>());
	}
	
	public boolean loadConfig(Document document, Collection<String> requiredEntities) {
		if (requiredEntities == null)
			requiredEntities = new ArrayList<String>();
		
		final NodeCollection fields = new NodeCollection(
			document.getDocumentElement().getChildNodes()	
		);
		for (Node field : fields) {
			if (field.getNodeType() == Node.ELEMENT_NODE) {
				final String name = DocumentUtils.impl.getAttribute(field, "name");
				final String value = field.getTextContent();
				
				if (!requiredEntities.remove(name) || !setProperty(name, value))
					break;
			}
		}
		
		return requiredEntities.isEmpty();
	}
	
	public String put(String key, String value) {
		return super.put(key, value.trim());
	}
	
	public String toXML() {
		return toXML(new ArrayList<String>());
	}
	
	public String toXML(Collection<String> exclude) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<root>");
		for (Map.Entry<String, String> entry : entrySet()) {
			if (!exclude.contains(entry.getKey())) {
				builder.append("<field name=\"" + entry.getKey() + "\">");
				builder.append("<![CDATA[" + entry.getValue() + "]]>");
				builder.append("</field>");
			}
		}
		builder.append("</root>");
		return builder.toString();
	}

}
