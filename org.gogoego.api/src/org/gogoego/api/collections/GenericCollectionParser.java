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

import java.util.Collection;

import org.w3c.dom.Document;

/**
 * GenericCollectionParser.java
 * 
 * Grants access to specific function in a collection to allow the 
 * parser to modify it.
 * 
 * This class is meant to be extended by different version of 
 * collections, and should handle read and write operations 
 * specific to that version.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class GenericCollectionParser {
	
	private final GenericCollection collection;
	
	public GenericCollectionParser(GenericCollection collection) {
		this.collection = collection;
	}
	
	/**
	 * Determine if this document contains valid XML for 
	 * this version.  Probably want to validate this against 
	 * a schema file.
	 * 
	 * @param document the document
	 * @return
	 */
	public abstract boolean isValid(Document document);
	
	/**
	 * Parse the document, and use the setter operations 
	 * to update the collection backing the parser.
	 * @param document the document to parse
	 */
	public abstract void parse(Document document);
	
	protected final String getCollectionID() {
		return collection.getCollectionID();
	}
	
	protected final void setCollectionID(String collectionID) {
		collection.setCollectionID(collectionID);
	}
	
	protected final String getCollectionName() {
		return collection.getName();
	}
	
	protected final void setCollectionName(String collectionName) {
		collection.setName(collectionName);
	}
	
	protected final String getCollectionDescription() {
		return collection.getCollectionDescription();
	}
	
	protected final void setCollectionDescription(String description) {
		collection.setCollectionDescription(description);
	}
	
	protected final String getCollectionKeywords() {
		return collection.getCollectionKeywords();
	}
	
	protected final void setCollectionKeywords(String keywords) {
		collection.setCollectionKeywords(keywords);
	}
	
	protected final String getCollectionType() {
		return collection.getCollectionType();
	}
	
	protected final void setCollectionType(String collectionType) {
		collection.setCollectionType(collectionType);
	}
	
	protected final String getCollectionAccessURI() {
		return collection.getCollectionAccessURI();
	}
	
	protected final void addSubCollection(GoGoEgoCollection subCollection) {
		collection.addSubCollection(subCollection);
	}
	
	protected final Collection<GoGoEgoCollection> getSubCollections() {
		return collection.getSubCollections().values();
	}
	
	protected final void addItem(GoGoEgoItem item) {
		collection.addItem(item);
	}
	
	protected final Collection<GoGoEgoItem> getItems() {
		return collection.getItems().values();
	}
	
	protected final void addCustomFieldData(CustomFieldData data) {
		collection.addCustomFieldData(data);
	}
	
	protected final String getCustomFieldsXML() {
		return collection.getCustomFieldsXML();
	}
	
	protected final void addOption(String optionName, String optionValue) {
		collection.addOption(optionName, optionValue);
	}
	
	protected final String getOptionsXML() {
		return collection.getOptionsXML();
	}

	/**
	 * Write the collection to XML according to this version's format.
	 * @return
	 */
	public abstract String write();
	
}
