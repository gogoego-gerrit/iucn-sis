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
package com.solertium.lwxml.shared.utils;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNamedNodeMap;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * Rudimentary serializer for the NativeDocument API, mostly for debugging purposes. 
 * Recursively serializes the NativeDocument, including attributes. If a Node has 
 * both text content and child nodes, ONLY the text content will be serialized, 
 * and the children will be bypassed. This behavior may change in the future.
 * 
 * The performance profile is not good, as it is a recursive solution, so I would 
 * HIGHLY suggest not using this for production ... only for testing and debugging. 
 * 
 * @author adam.schwartz@solertium.com
 */
public class NativeDocumentSerializer {

	private static short ELEMENT_NODE = 1;
	
	public static String serialize(NativeDocument ndoc) { 
		return serialize(ndoc.getDocumentElement());
	}

	public static String serialize(NativeNode root) {
		StringBuilder ret = new StringBuilder();
		return serializeRecursive(root, ret, 0);
	}

	private static String serializeRecursive(NativeNode el, StringBuilder ret, int depth) {
		addTabs(ret, depth);

		ret.append("<");
		ret.append(el.getNodeName());

		if (el instanceof NativeElement) {
			NativeNamedNodeMap attrs = ((NativeElement)el).getAttributes();
			for( int j = 0; j < attrs.getLength(); j++ ) {
				NativeNode curAttr = attrs.item(j);
				ret.append(" " + curAttr.getNodeName());
				if( curAttr.getNodeValue() != null && !curAttr.equals("")) {
					ret.append("=\"");
					ret.append(curAttr.getNodeValue());
					ret.append("\"");
				}
			}
		}

		String txt = el.getTextContent();
		NativeNodeList children = el.getChildNodes();
		
		if( txt != null && !txt.matches("\\s*")) {
			ret.append(">");
			ret.append(txt);
			ret.append("</");
			ret.append(el.getNodeName());
			ret.append(">");
			ret.append("\r\n");
		} else if( children.getLength() > 0 ) {
			ret.append(">");
			ret.append("\r\n");
			
			for( int i = 0; i < children.getLength(); i++ ) {
				if( children.item(i).getNodeType() == ELEMENT_NODE )
					serializeRecursive(children.elementAt(i), ret, depth+1);
			}
			
			addTabs(ret, depth);
			ret.append("</");
			ret.append(el.getNodeName());
			ret.append(">");
			ret.append("\r\n");
		} else {
			ret.append("/>");
			ret.append("\r\n");
		}

		return ret.toString();
	}

	/**
	 * Adds "tabs." Right now it just adds 4 spaces.
	 * 
	 * @param ret - string to append "tabs" to
	 * @param depth - how many "tabs" to add
	 */
	private static void addTabs(StringBuilder ret, int depth) {
		for( int j = 0; j < depth; j++ )
			ret.append("    ");
	}


}
