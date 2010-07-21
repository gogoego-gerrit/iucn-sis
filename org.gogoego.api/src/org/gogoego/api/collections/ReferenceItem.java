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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * ReferenceItem.java
 * 
 * A reference item is nothing more than a reference to some other existing item
 * in some other place in the system. It holds only the item name and the uri of
 * the item it references.
 * 
 * The system will know how to handle operations when this item is found, such
 * as deleting. Must be careful when basing decisions on the parent collection
 * uri or name, however, because the URI of this item may not have any bearing
 * on it and cause unexpected/incorrect results.
 * 
 * These items should also never be saved to disk, they exists only as reference
 * links in a collection
 * 
 * @author carl.scott
 * 
 */
public class ReferenceItem extends GoGoEgoItem {

	private String itemURI;

	public ReferenceItem(String itemName, String itemURI) {
		super(null, itemName);
		this.itemURI = itemURI;
	}

	public ReferenceItem(Document document) {
		super(null, null);
		convertFromXMLDocument(document);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((itemName == null) ? 0 : itemName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ReferenceItem other = (ReferenceItem) obj;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
		return true;
	}

	public void convertFromXMLDocument(Document document) {
		final Node root = document.getFirstChild();
		if (root.getNodeName().equalsIgnoreCase("referenceItem")) {
			itemID = null;

			final NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				final Node current = children.item(i);
				if (current.getNodeName().equalsIgnoreCase("name"))
					itemName = current.getTextContent();
				else if (current.getNodeName().equalsIgnoreCase("uri")) {
					itemURI = current.getTextContent();
				}
			}
		}
	}

	public String getItemURI() {
		return itemURI;
	}

	public String toXML() {
		return "<referenceItem>" + "<name>" + itemName + "</name>" + "<uri>" + itemURI + "</uri>" + "</referenceItem>";
	}

}
