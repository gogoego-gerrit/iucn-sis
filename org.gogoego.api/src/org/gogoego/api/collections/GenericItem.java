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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.portable.XMLWritingUtils;

/**
 * GenericItem.java
 * 
 * A generic item having a name, id, template, and a number of custom fields. No
 * content assertions are made.
 * 
 * @author carl.scott
 * 
 */
public class GenericItem extends GoGoEgoItem {

	private final LinkedHashMap<String, CustomFieldData> data;

	public GenericItem(final Document document) {
		super("");
		data = new LinkedHashMap<String, CustomFieldData>();
		convertFromXMLDocument(document);
	}

	public GenericItem(final String itemID, final String itemName) {
		super(itemID, itemName);
		data = new LinkedHashMap<String, CustomFieldData>();
	}

	/**
	 * Converts an item to object. If the root node is not "item", I dont care,
	 * I'm not processing it.
	 */
	public void convertFromXMLDocument(final Document document) {
		final Node root = document.getFirstChild();
		if (root.getNodeName().equalsIgnoreCase("item")) {
			itemID = DocumentUtils.impl.getAttribute(root, "id");

			final NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				final Node current = children.item(i);
				if (current.getNodeName().equalsIgnoreCase("name"))
					itemName = current.getTextContent();
				else if (current.getNodeName().equalsIgnoreCase("custom")) {
					final CustomFieldData cfd = CustomFieldData.buildCustomFieldDataFromNode(current);
					data.put(cfd.getName(), cfd);
				}
			}
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GenericItem other = (GenericItem) obj;
		if (((itemID == null) && (other.itemID != null))
				|| ((itemID != null) && !itemID.equalsIgnoreCase(other.itemID)))
			return false;
		if (((itemName == null) && (other.itemName != null))
				|| ((itemName != null) && !itemName.equalsIgnoreCase(other.itemName)))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return (itemID + itemName + data).hashCode();
	}

	public String getData(String key) {
		try {
			return data.containsKey(key) ? data.get(key).getValue() : null;
		} catch (NullPointerException impossible) {
			return null;
		}
	}
	
	public Map<String, CustomFieldData> getAllData() {
		return data;
	}

	public String toXML() {
		StringBuffer xml = new StringBuffer();
		xml.append("<item id=\"" + itemID + "\">\r\n");
		xml.append(XMLWritingUtils.writeCDATATag("name", itemName) + "\r\n");
		final Iterator<CustomFieldData> iterator = data.values().iterator();
		while (iterator.hasNext())
			xml.append(iterator.next().toXML());
		xml.append("</item>");
		return xml.toString();
	}

}
