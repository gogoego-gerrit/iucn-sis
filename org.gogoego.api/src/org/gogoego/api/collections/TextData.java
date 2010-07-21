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
package org.gogoego.api.collections;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.Replacer;
import com.solertium.util.portable.XMLWritingUtils;

/**
 * TextData.java
 * 
 * Implementation of custom field data that stores text.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class TextData extends CustomFieldData {

	private String text;
	private String type;

	public TextData(final String name, final String text) {
		super(name);
		this.text = text;
	}

	public TextData(final String name, final String text, final String type) {
		super(name);
		this.text = text;
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public String getValue() {
		return getText();
	}

	@Override
	public Element toHTML(final Document document) {
		final Element element = document.createElement("div");

		final Element nameNode = document.createElement("span");
		nameNode.setTextContent(fieldName + ": ");

		final Element textNode = document.createElement("span");
		textNode.setTextContent(Replacer.escapeTags(text));

		element.appendChild(nameNode);
		element.appendChild(textNode);

		return element;
	}

	@Override
	public String toXML() {
		StringBuffer xml = new StringBuffer();
		if (type == null)
			xml.append("<custom name=\"" + fieldName + "\">\r\n");
		else
			xml.append("<custom name=\"" + fieldName + "\" type=\"" + type + "\">\r\n");
		xml.append(XMLWritingUtils.writeCDATATag("text", Replacer.compressSpaces(text)) + "\r\n");
		xml.append("</custom>\r\n");
		return xml.toString();
	}

	public String getType() {
		return type;
	}

}
