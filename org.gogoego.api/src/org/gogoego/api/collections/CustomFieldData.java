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

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * CustomFieldData.java
 * 
 * Implemenation for simply custom field data type.  Currently, 
 * only text is supported.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class CustomFieldData {

	public static CustomFieldData buildCustomFieldDataFromNode(final Node node) {
		if (!node.getNodeName().equalsIgnoreCase("custom"))
			return null;

		final NodeList nodes = node.getChildNodes();
		final String name = DocumentUtils.impl.getAttribute(node, "name");
		final String type = DocumentUtils.impl.getAttribute(node, "type");
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node current = nodes.item(i);
			if (current.getNodeName().equalsIgnoreCase("text"))
				if (type.equalsIgnoreCase(""))
					return new TextData(name, current.getTextContent());
				else
					return new TextData(name, current.getTextContent(), type);

		}

		return null;
	}

	protected String fieldName;

	public CustomFieldData(final String fieldName) {
		this.fieldName = fieldName;
	}

	public String getName() {
		return fieldName;
	}

	public abstract String getValue();

	/**
	 * Creates an HTML representation of the data
	 * 
	 * @param document
	 *            the document the html will go into
	 * @return an element to insert
	 */
	public abstract Element toHTML(Document document);

	@Override
	public String toString() {
		return toXML();
	}

	public abstract String toXML();

	public abstract String getType();
}
