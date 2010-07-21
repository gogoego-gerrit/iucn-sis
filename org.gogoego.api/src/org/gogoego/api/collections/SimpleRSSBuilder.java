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

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.data.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.restlet.InternalRequest;
import com.solertium.vfs.VFSPath;

/**
 * SimpleRSSBuilder.java
 * 
 * Creates an RSS Document from a given collection.
 * 
 * @author carl.scott
 * 
 */
public abstract class SimpleRSSBuilder {

	protected final HashMap<String, String> data;
	protected final Request request;
	protected final VFSPath relativeAccessURI;
	protected final GoGoEgoCollection category;
	protected final String hostIdentifier;

	/**
	 * Constructor
	 * 
	 * @param request
	 *            the request
	 * @param relativeAccessURI
	 *            the public relative access uri (i.e. /collections)
	 * @param category
	 *            the current category
	 */
	public SimpleRSSBuilder(final Request request, final VFSPath relativeAccessURI, final GoGoEgoCollection category) {
		this.request = request;
		this.relativeAccessURI = relativeAccessURI;
		this.category = category;
		this.data = new HashMap<String, String>();

		Request top = request;
		if (top instanceof InternalRequest)
			top = ((InternalRequest) top).getFirstRequest();
		this.hostIdentifier = top.getResourceRef().getHostIdentifier();
	}

	/**
	 * Set what piece of data to use for the RSS item title
	 * 
	 * @param field
	 *            the field
	 */
	public void setTitleField(String field) {
		setField("title", field);
	}

	/**
	 * Set what piece of data to use for the RSS item description
	 * 
	 * @param field
	 *            the field
	 */
	public void setDescriptionField(String field) {
		setField("description", field);
	}

	/**
	 * Set what piece of data to use for the item author
	 * 
	 * @param field
	 *            the field
	 */
	public void setAuthorField(String field) {
		setField("author", field);
	}

	/**
	 * Sets an arbitrary field for the RSS builder to lookup at the item level
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setField(String key, String value) {
		data.put(key, value);
	}

	/**
	 * Creates the RSS Document
	 * 
	 * @return the document
	 */
	public Document build() {
		final Document doc = DocumentUtils.impl.newDocument();

		final Element root = doc.createElement("rss");
		root.setAttribute("version", "2.0");

		final Element channel = doc.createElement("channel");
		{
			channel.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "title", hostIdentifier + " - "
					+ category.getName()));
			channel.appendChild(DocumentUtils.impl.createElementWithText(doc, "link", hostIdentifier
					+ relativeAccessURI));
			channel.appendChild(DocumentUtils.impl.createCDATAElementWithText(doc, "description", category.getName()));
			channel.appendChild(DocumentUtils.impl.createElementWithText(doc, "language", "en-us"));
		}

		Iterator<GoGoEgoItem> iterator = category.getItems().values().iterator();
		while (iterator.hasNext()) {
			final GoGoEgoItem current = fetchAndBuildItem(iterator.next());
			if (current == null)
				continue;
			
			final Element item = doc.createElement("item");

			item.appendChild(DocumentUtils.impl.createCDATAElementWithText(
				doc, "title", getItemData("title", current)
			));
			
			String template = getItemData("template", current);
			if (template == null)
				template = "";			
			
			item.appendChild(DocumentUtils.impl.createElementWithText(
				doc, "link", getItemLink(current) + template 
			));
			
			if (data.containsKey("description"))
				item.appendChild(DocumentUtils.impl.createCDATAElementWithText(
					doc, "description", 
					getItemData("description", current)
				));
			
			if (data.containsKey("author"))
				item.appendChild(DocumentUtils.impl.createCDATAElementWithText(
					doc, "author", getItemData("author", current)
				));

			channel.appendChild(item);
		}

		root.appendChild(channel);

		doc.appendChild(root);

		return doc;
	}

	/**
	 * Given an item from the category map, look up, load, or simply return the
	 * correct GoGoEgoItem for use.
	 * 
	 * @param item
	 *            the item, probably a pointer
	 * @return the item with data
	 */
	protected abstract GoGoEgoItem fetchAndBuildItem(GoGoEgoItem item);

	/**
	 * Retrieves a piece of data given a key. The key that was set up externally
	 * to locate this piece of data in the item should be set in the "data"
	 * HashMap.
	 * 
	 * An example use case:
	 * 
	 * return item.getData(data.get("key"))
	 * 
	 * @param key
	 *            the data key
	 * @param item
	 *            the item
	 * @return the requested data
	 */
	protected abstract String getItemData(String key, GoGoEgoItem item);

	/**
	 * Retrieves the public link to the given item
	 * 
	 * @param item
	 *            the item
	 * @return the link
	 */
	protected abstract String getItemLink(GoGoEgoItem item);
}
