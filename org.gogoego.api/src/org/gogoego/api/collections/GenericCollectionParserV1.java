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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.NodeCollection;
import com.solertium.util.Replacer;

/**
 * GenericCollectionParserV1.java
 * 
 * Legacy support for collections.  New sites will never use this, 
 * and old sites will discontinue use as they edit and save their 
 * existing collections.
 * 
 * Until then, this will support the old means of parsing collections.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GenericCollectionParserV1 extends GenericCollectionParser {
	
	public GenericCollectionParserV1(GenericCollection collection) {
		super(collection);
	}
	
	public boolean isValid(Document document) {
		return document.getFirstChild().getNodeName().equalsIgnoreCase("collection");
	}
	
	public void parse(Document document) {
		final Node root = document.getFirstChild();
		setCollectionID(DocumentUtils.impl.getAttribute(root, "id"));
		setCollectionName(DocumentUtils.impl.getAttribute(root, "name"));
		
		String collectionType = DocumentUtils.impl.getAttribute(root, "type");
		if (collectionType.equals(""))
			collectionType = null;
		setCollectionType(collectionType);
		
		final NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final Node current = children.item(i);
			if (current.getNodeName().equalsIgnoreCase("subcollection"))
				addSubCollection(new GenericCollection(DocumentUtils.impl.getAttribute(current, "id"),
						DocumentUtils.impl.getAttribute(current, "name")));
			else if (current.getNodeName().equalsIgnoreCase("item")) {
				String itemID = DocumentUtils.impl.getAttribute(current, "id");
				if (itemID.equals(""))
					addItem(new ReferenceItem(DocumentUtils.impl.getAttribute(current, "name"), DocumentUtils.impl
							.getAttribute(current, "uri")));
				else
					addItem(new GenericItem(DocumentUtils.impl.getAttribute(current, "id"), DocumentUtils.impl
							.getAttribute(current, "name")));
			} else if (current.getNodeName().equals("custom")) {
				addCustomFieldData(CustomFieldData.buildCustomFieldDataFromNode(current));
			} else if (current.getNodeName().equals("options")) {
				final NodeCollection optionNodes = new NodeCollection(current.getChildNodes());
				for (Node curChild : optionNodes)
					if (curChild.getNodeName().equals("option"))
						addOption(DocumentUtils.impl.getAttribute(curChild, "name"), curChild.getTextContent());
			}
		}
	}
	
	public String write() {
		final String collectionType = getCollectionType();
		String xml = "<collection id=\"" + getCollectionID() + "\" name=\"" + cleanAttributeText(getCollectionName())
		+ "\"" + (collectionType == null ? "" : " type=\"" + collectionType + "\"") + ">";
		
		for (GoGoEgoCollection current : getSubCollections()) {
			xml += "<subcollection id=\"" + current.getCollectionID() + "\" name=\""
				+ cleanAttributeText(current.getName()) + "\" " + "uri=\""
				+ current.getCollectionAccessURI() + "\" />";
		}
		for (GoGoEgoItem current : getItems()) {
			if (current instanceof ReferenceItem) {
				xml += "<item name=\"" + cleanAttributeText(current.getItemName()) + "\" " + "uri=\""
					+ ((ReferenceItem) current).getItemURI() + "\" />";
			} else {
				xml += "<item id=\"" + current.getItemID() + "\" name=\""
						+ cleanAttributeText(current.getItemName()) + "\" " + "uri=\""
						+ getCollectionAccessURI() + "/" + current.getItemID() + "\" />";
			}
		}
		xml += getCustomFieldsXML();
		xml += getOptionsXML();
		xml += "</collection>";
		return xml;
	}
	
	/*
	 * FIXME: this piece of legacy code will not 
	 * work.  Changes need to be made to the XML 
	 * structure to support the use of ampersands 
	 * and other attribute-level illegal characters.
	 */
	private String cleanAttributeText(String in) {
		return Replacer.escapeTags(in);
	}

}
