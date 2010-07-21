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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.solertium.util.MD5Hash;
import com.solertium.vfs.VFSPath;

/**
 * GoGoEgoCollection.java
 * 
 * Abstraction of a standard GoGoEgo collection object.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class GoGoEgoCollection implements GoGoEgoXMLObject {

	protected String collectionID;
	protected String collectionType;
	protected String collectionDescription;
	protected String collectionKeywords;
	protected VFSPath fileLocation;

	protected LinkedHashMap<String, GoGoEgoItem> items;
	protected String name;
	protected GoGoEgoCollection parent;

	protected LinkedHashMap<String, GoGoEgoCollection> subCollections;
	protected LinkedHashMap<String, CustomFieldData> data;
	protected LinkedHashMap<String, String> options;

	public GoGoEgoCollection(final String collectionID) {
		this(collectionID, "");
	}

	public GoGoEgoCollection(final String collectionID, final String name) {
		subCollections = new LinkedHashMap<String, GoGoEgoCollection>();
		items = new LinkedHashMap<String, GoGoEgoItem>();
		data = new LinkedHashMap<String, CustomFieldData>();
		options = new LinkedHashMap<String, String>();

		this.collectionID = collectionID;
		this.name = name;

		parent = null;
	}

	/** ******* UTILITY FUNCTIONS ******* */

	public void addItem(final GoGoEgoItem item) {
		if (item.getItemID() != null)
			items.put(item.getItemID(), item);
		else
			items.put("$RI$" + new MD5Hash(((ReferenceItem) item).getItemURI()).toString(), item);
	}

	public void addSubCollection(final GoGoEgoCollection subCollection) {
		subCollections.put(subCollection.getCollectionID(), subCollection);
		subCollection.setParent(this);
	}

	/**
	 * Returns a public URI for this collection. This is how you access a
	 * collection via URI
	 * 
	 * @return the uri with a leading slash
	 */
	public String getCollectionAccessURI() {
		String uri = collectionID;
		GoGoEgoCollection current = parent;
		while (current != null) {
			uri = current.getCollectionID() + "/" + uri;
			current = current.parent;
		}
		return "/" + uri;
	}

	public VFSPath getCollectionFileLocation() {
		return fileLocation;
	}

	public String getCollectionID() {
		return collectionID;
	}

	public HashMap<String, GoGoEgoItem> getItems() {
		return items;
	}

	public String getName() {
		return name;
	}

	public GoGoEgoCollection getParent() {
		return parent;
	}
	
	public String getCollectionDescription() {
		return collectionDescription;
	}
	
	public String getCollectionKeywords() {
		return collectionKeywords;
	}

	public String getCollectionType() {
		return collectionType;
	}

	/** **** GETTERS AND SETTERS ********** */

	public HashMap<String, GoGoEgoCollection> getSubCollections() {
		return subCollections;
	}

	public GoGoEgoItem removeItem(final String itemID) {
		return items.remove(itemID);
	}

	public GoGoEgoCollection removeSubCollection(final String collectionID) {
		return subCollections.remove(collectionID);
	}

	public void setCollectionFileLocation(final VFSPath fileLocation) {
		this.fileLocation = fileLocation;
	}

	public void setCollectionID(final String collectionID) {
		this.collectionID = collectionID;
	}
	
	public void setCollectionKeywords(final String keywords) {
		this.collectionKeywords = keywords;
	}

	public void setCollectionType(final String collectionType) {
		this.collectionType = "".equals(collectionType) ? null : collectionType;
	}
	
	public void setCollectionDescription(final String description) {
		this.collectionDescription = description;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setParent(final GoGoEgoCollection parent) {
		this.parent = parent;
	}

	protected final String getOptionsXML() {
		if (options.isEmpty())
			return "";

		final StringBuilder builder = new StringBuilder();
		builder.append("<options>");
		final Iterator<String> iterator = options.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			builder.append("<option name=\"" + key + "\"><![CDATA[" + options.get(key) + "]]></option>");
		}
		builder.append("</options>");
		return builder.toString();
	}

	protected final String getCustomFieldsXML() {
		final StringBuilder builder = new StringBuilder();
		final Iterator<CustomFieldData> custom = data.values().iterator();
		while (custom.hasNext())
			builder.append(custom.next().toXML());
		return builder.toString();
	}

	public abstract String toXML();
}
