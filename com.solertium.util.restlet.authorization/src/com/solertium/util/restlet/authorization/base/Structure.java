/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util.restlet.authorization.base;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.util.SchemaValidator;
import com.solertium.util.portable.XMLWritingUtils;

public class Structure {
	
	private final Map<String, AuthorizableObject> map;
	
	public Structure(final Document document) throws IllegalArgumentException {
		map = new HashMap<String, AuthorizableObject>();
		
		parse(document);
	}
	
	public void parse(final Document document) throws IllegalArgumentException {
		if (!doesfollowSchema(document)) {
			System.err.println("This document does not follow schema and will not be parsed!");
			return;
		}
		
		final Map<String, ArrayList<String>> inheritance = 
			new HashMap<String, ArrayList<String>>(); 
		
		final ElementCollection nodes = new ElementCollection(
			document.getElementsByTagName("object")	
		);
		for (Element e : nodes) {
			final String type = e.getAttribute("type");
			if (type == null)
				continue;
			
			final BaseAuthorizableObject obj = new BaseAuthorizableObject();
			final NodeCollection children = new NodeCollection(
				e.getChildNodes()
			);
			for (Node curChild : children) {
				if ("uri".equals(curChild.getNodeName()))
					obj.addUri(curChild.getTextContent());
				else if ("action".equals(curChild.getNodeName()))
					obj.addAction(curChild.getTextContent());
				else if ("inherits".equals(curChild.getNodeName())) {
					ArrayList<String> list = inheritance.get(type);
					if (list == null) 
						list = new ArrayList<String>();
					list.add(curChild.getTextContent());
					inheritance.put(type, list);
				}
			}
			
			map.put(type, obj);
		}
		
		for (Map.Entry<String, ArrayList<String>> entry : inheritance.entrySet()) {
			final AuthorizableObject obj = map.get(entry.getKey());
			if (obj == null)
				continue;
			
			for (String curType : entry.getValue()) {
				final AuthorizableObject current = map.get(curType);
				if (current == null)
					continue;
				
				for (String action : current.getAllowedActions())
					((BaseAuthorizableObject)obj).addAction(action);
			}
		}
	}
	
	public Map<String, AuthorizableObject> getMapping() {
		return map;
	}
	
	public static boolean doesfollowSchema(final Document document) {
		return SchemaValidator.isValid(
			Structure.class.getResourceAsStream("struct.xsd"), 
			new StringReader(BaseDocumentUtils.impl.serializeDocumentToString(document))
		);
	}
	
	/**
	 * Returns the full XML as interpreted upon compilation, 
	 * so the inherits information is already injected.
	 * @return
	 */
	public String toFullXML() {
		StringBuilder xml = new StringBuilder();
		
		xml.append("<structure>");
		
		for (Map.Entry<String, AuthorizableObject> entry : map.entrySet()) {
			xml.append("<object type=\"" + entry.getKey() + "\">");
			for (String uri : entry.getValue().getUris())
				xml.append(XMLWritingUtils.writeTag("uri", uri));
			for (String action : entry.getValue().getAllowedActions())
				xml.append(XMLWritingUtils.writeTag("action", action));
			xml.append("</object>");
		}
		
		xml.append("</structure>");
		
		return xml.toString();
	}

}
