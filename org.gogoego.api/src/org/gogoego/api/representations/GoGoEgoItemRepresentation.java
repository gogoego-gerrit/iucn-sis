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
package org.gogoego.api.representations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gogoego.api.collections.GenericItem;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.CSVTokenizer;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.vfs.VFSPath;

/**
 * GoGoEgoItemRepresentation.java
 * 
 * The GoGoEgo Item representation. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoEgoItemRepresentation extends GoGoEgoStringRepresentation implements HasURI {
	
	private final HashMap<String, String> cache;
	private GenericItem item;
	private VFSPath parent;
	private Request request;
	private Context context;
	
	public GoGoEgoItemRepresentation(final Request request, final Context context, final VFSPath parent, final String content) {
		super(content, MediaType.TEXT_XML);
		cache = new HashMap<String, String>();
		this.parent = parent;
		this.request = request;
		this.context = context;
	}
	
	public GoGoEgoItemRepresentation(final Request request, final Context context, final VFSPath parent, final GenericItem item) {
		this(request, context, parent, item.toXML());
		this.item = item;
	}
	
	public Trap<GoGoEgoItemRepresentation> newTrap() {
		return new GoGoEgoItemRepresentationTrap(this);
	}
	
	public String getURI() {
		return resolveEL("uri");
	}

	public GoGoEgoCollectionRepresentation getParent() {
		String reference = "riap://host" + parent.toString();
		Request riap = new InternalRequest(request, Method.GET, reference, null);

		String content = null;
		try {
			content = GoGoEgo.get().getFromContext(context).getVFS().getString(parent);	
		} catch (IOException e) {
			e.printStackTrace();
			content = null;
		}
		
		return new GoGoEgoCollectionRepresentation(riap, context, content);
	}

	public String getContentType() {
		return "collection/item";
	}

	private GenericItem getItem() {
		if (item == null)
			item = new GenericItem(getDocument());
		return item;
	}

	public String getItemID() {
		return getItem().getItemID();
	}

	public String getItemName() {
		return getItem().getItemName();
	}

	public String getValue(final String key) {
		if (cache.isEmpty()) {
			final Document document = getDocument();
			final NodeList children = document.getFirstChild().getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				final Node current = children.item(i);
				if (current.getNodeName().equalsIgnoreCase("custom")) {
					final NodeList nodes = current.getChildNodes();
					for (int j = 0; j < nodes.getLength(); j++) {
						final Node custom = nodes.item(j);
						if (custom.getNodeName().equalsIgnoreCase("text"))
							cache.put(DocumentUtils.impl.getAttribute(current, "name"), custom.getTextContent());
					}
				}
			}
		}

		return cache.containsKey(key) ? cache.get(key) : "";
	}

	public String keySetCSV() {
		getValue("");
		String ret = "";
		final Iterator<String> iterator = cache.keySet().iterator();
		while (iterator.hasNext())
			ret += iterator.next() + (iterator.hasNext() ? "," : "");
		return ret;
	}

	@Override
	public String resolveEL(final String key) {
		if (key.equals("itemName"))
			return getItem().getItemName();
		else if (key.equals("itemID"))
			return getItem().getItemID();
		else if (key.equals("uri"))
			return request.getResourceRef().getPath();
		else if (key.equals("parentURI"))
			return parent.getCollection().toString().substring(9);

		String value = getValue(key);
		if (cache.containsKey(key))
			return value;
		else
			return super.resolveEL(key);
	}
	
	public String resolveConditionalEL(String template, String keyCSV) {
		final Map<String, String> resolved = new HashMap<String, String>();
		final CSVTokenizer tokenizer = new CSVTokenizer(keyCSV);
		tokenizer.setNullOnEnd(true);
		
		String key;
		while ((key = tokenizer.nextToken()) != null) {
			String value = resolveEL(key);
			if (value == null || (value.equals("") && !cache.containsKey(key)))
				return "";
			else
				resolved.put(key, value);
		}
		
		return substitute(template, resolved.values());
	}

}
