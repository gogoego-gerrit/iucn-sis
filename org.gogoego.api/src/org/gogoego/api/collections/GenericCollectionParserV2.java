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

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;
import com.solertium.util.portable.XMLWritingUtils;

/**
 * GenericCollectionParserV2.java
 * 
 * New version of collections:
 * 
 * Enhancements:
 *  - Added inherent "description" field
 *  - Added inherent "keywords" field 
 *  
 * Fixes:
 * - Moved item and collection name from node attribute to node 
 *  	text content, wrapped in CDATA, to allow user free input 
 *  	for the item and collection names (attributes are too 
 *  	sensitive for this and cause problems in V1) 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GenericCollectionParserV2 extends GenericCollectionParser {
	
	public GenericCollectionParserV2(GenericCollection collection) {
		super(collection);
	}

	/**
	 * TODO: validate against schema
	 */
	public boolean isValid(Document document) {
		return document.getFirstChild().getNodeName().equalsIgnoreCase("collection") && 
			"2".equals(document.getDocumentElement().getAttribute("version"));
	}

	public void parse(Document document) {
		final Element root = document.getDocumentElement();
		setCollectionID(BaseDocumentUtils.impl.getAttribute(root, "id"));
		String collectionType = DocumentUtils.impl.getAttribute(root, "type");
		if (collectionType.equals(""))
			collectionType = null;
		setCollectionType(collectionType);
		
		final NodeCollection nodes = new NodeCollection(root.getChildNodes());
		for (Node current : nodes) {
			if ("name".equals(current.getNodeName()))
				setCollectionName(current.getTextContent());			
			else if ("description".equals(current.getNodeName()))
				setCollectionDescription(current.getTextContent());
			else if ("keywords".equals(current.getNodeName()))
				setCollectionKeywords(current.getTextContent());
			else if ("subcollection".equalsIgnoreCase(current.getNodeName()))
				addSubCollection(new GenericCollection(BaseDocumentUtils.impl.getAttribute(current, "id"), current.getTextContent()));
			else if ("item".equals(current.getNodeName())) {
				final String itemID = BaseDocumentUtils.impl.getAttribute(current, "id");
				if ("".equals(itemID))
					addItem(new ReferenceItem(current.getTextContent(), BaseDocumentUtils.impl.getAttribute(current, "uri")));
				else
					addItem(new GenericItem(itemID, current.getTextContent()));
			}
			else if ("custom".equals(current.getNodeName()))
				addCustomFieldData(CustomFieldData.buildCustomFieldDataFromNode(current));
			else if ("options".equals(current.getNodeName())) {
				final NodeCollection optionNodes = new NodeCollection(current.getChildNodes());
				for (Node curChild : optionNodes)
					if (curChild.getNodeName().equals("option"))
						addOption(DocumentUtils.impl.getAttribute(curChild, "name"), curChild.getTextContent());
			}
		}
	}
	
	public String write() {
		final String collectionType = getCollectionType();
		final StringBuilder xml = new StringBuilder();
		xml.append("<collection id=\"" + getCollectionID() + "\" version=\"2\"" + 
			(collectionType == null ? "" : " type=\"" + collectionType + "\"") + ">");
		xml.append(XMLWritingUtils.writeCDATATag("name", getCollectionName()));
		xml.append(XMLWritingUtils.writeCDATATag("description", getCollectionDescription()));
		xml.append(XMLWritingUtils.writeCDATATag("keywords", getCollectionKeywords()));
		
		for (GoGoEgoCollection current : getSubCollections())
			xml.append("<subcollection id=\"" + current.getCollectionID() + "\" uri=\"" + current.getCollectionAccessURI() + "\"><![CDATA[" + current.getName() + "]]></subcollection>");
		
		for (GoGoEgoItem current : getItems()) 
			if (current instanceof ReferenceItem) 
				xml.append("<item uri=\"" + ((ReferenceItem)current).getItemURI() + "\"><![CDATA[" + current.getItemName() + "]]></item>");
			else
				xml.append("<item id=\"" + current.getItemID() + "\" uri=\"" + getCollectionAccessURI() + "/" + current.getItemID() + "\"><![CDATA[" + current.getItemName() + "]]></item>");

		xml.append(getCustomFieldsXML());
		xml.append(getOptionsXML());
		xml.append("</collection>");
		
		return xml.toString();
	}

}
