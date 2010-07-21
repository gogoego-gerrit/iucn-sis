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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.gogoego.api.collections.CollectionCache;
import org.gogoego.api.collections.CollectionResourceBuilder;
import org.gogoego.api.collections.GenericCollection;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.AlphanumericComparator;
import com.solertium.util.BaseTagListener;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.TagFilter.Tag;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.vfs.VFSPath;

/**
 * GoGoEgoCollectionRepresentation.java
 * 
 * Represents a GoGoEgo collection
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoEgoCollectionRepresentation extends GoGoEgoStringRepresentation implements HasURI {
	
	protected final Request request;
	protected final Context context;

	protected ArrayList<VFSPath> itemURIs;
	protected ArrayList<VFSPath> collectionURIs;

	protected HashMap<String, String> cache;

	protected static final AlphanumericComparator comparator = new AlphanumericComparator();
	
	private final CollectionResourceBuilder builder;
	private GenericCollection object = null;
	
	public GoGoEgoCollectionRepresentation(final Request request, final Context context, final String content) {
		super(content, MediaType.TEXT_XML);
		this.context = context;
		this.request = request;
		cache = new HashMap<String, String>();
		
		/*
		 * TODO: clean this process up.
		 */
		builder = new CollectionResourceBuilder(context);
	}
	
	public String getContentType() {
		return "collection/category";
	}

	/**
	 * Creates a default grid view of the collection's object as best it can.
	 * 
	 * @param template
	 *            the template to use for each item
	 * @return the HTML for the grid
	 */
	public String getCollectionAsGrid(final String itemTemplate) {
		return getCollectionAsGrid(itemTemplate, 3);
	}
	
	/**
	 * Creates a grid view of the collection's object given the item's template
	 * and number of items per row.
	 * 
	 * @param template
	 *            the template to use for each item
	 * @param itemsPerRow
	 *            the number of items per row
	 * @return the HTML for the grid
	 */
	public String getCollectionAsGrid(final String template, final int itemsPerRow) {
		return getCollectionAsGrid(template, itemsPerRow, -1);
	}

	public String getCollectionAsGrid(final String template, final int itemsPerRow, int numItemsToReturn) {
		final StringWriter out = new StringWriter();

		int numPrinted = 0;
		if (numItemsToReturn < 0)
			numItemsToReturn = getItemURIs().size();

		for (int i = 0; (i < getItemURIs().size() && numPrinted < numItemsToReturn); i++) {
			out.append("<gogo:marker gclass=\"collection/item\" src=\"" + getItemURIs().get(i) + "\" />");
			out.append("\r\n<div>\r\n${" + getItemURIs().get(i) + "/" + template + "}\r\n</div>\r\n");
			
			if (++numPrinted % itemsPerRow == 0)
				out.write("<div style=\"clear:both;height:20px\"></div>");
		}

		return out.toString();
	}
	
	public String getCollectionAsList(String itemTemplate) {
		final StringBuilder out = new StringBuilder();
		
		for (int i = 0; i < getItemURIs().size(); i++) {
			//try {
				/*final GoGoEgoBaseRepresentation representation = 
					GoGoEgo.get().applyTemplating(itemTemplate, getItemObject(i));*/
				out.append("<gogo:marker gclass=\"collection/item\" src=\"" + getItemURIs().get(i) + "\" />");
				out.append("\r\n<div>\r\n${" + getItemURIs().get(i) + "/" + itemTemplate + "}\r\n</div>\r\n");
				//out.append(representation.getContent());
			/*} catch (NotFoundException e) {
				return "<!-- Template " + itemTemplate + " could not be found -->";
			} catch (Exception e) {
				return "<!-- " + e.getMessage() + "-->";
			}*/
		}
		
		return out.toString();
	}

	/**
	 * Returns the name of the collection
	 * 
	 * @return the name
	 */
	public String getCollectionName() {
		return getObject().getName();
	}

	/**
	 * Returns the id of the collection
	 * 
	 * @return the id
	 */
	public String getCollectionID() {
		return getObject().getCollectionID();
	}
	
	public String getDescription() {
		return getObject().getCollectionDescription();
	}
	
	public String getKeywords() {
		return getObject().getCollectionKeywords();
	}
	
	private GenericCollection getObject() {
		if (object == null)
			object = new GenericCollection(getDocument());
		return object;
	}

	public int getItemCount() {
		return getItemURIs().size();
	}

	public int getCollectionCount() {
		return getCollectionURIs().size();
	}

	public GoGoEgoItemRepresentation getItemObject(final int index) {
		if (itemURIs == null)
			getItemURIs();

		final String itemURI = getItemURI(index);
		if (itemURI.equals(""))
			return null;

		final GoGoEgoItemRepresentation representation;
		if (CollectionCache.getInstance().isCached(context, itemURI))
			representation = (GoGoEgoItemRepresentation) 
				CollectionCache.getInstance().getRepresentation(context, request, itemURI);
		else {
			representation = (GoGoEgoItemRepresentation) builder.handleGet(request, new VFSPath(itemURI));
		}
		
		return representation;
	}

	public GoGoEgoCollectionRepresentation getCollectionObject(final int index) {
		if (collectionURIs == null)
			getCollectionURIs();

		final String collectionURI = getCollectionURI(index);
		if (collectionURI.equals(""))
			return null;

		GoGoEgoCollectionRepresentation child =
			(GoGoEgoCollectionRepresentation) builder.handleGet(request, new VFSPath(collectionURI));
		
		return child;
	}

	public String getItemURI(final int index) {
		return ((itemURIs != null) && ((index >= 0) && (index < itemURIs.size()))) ? itemURIs.get(index).toString()
				: "";
	}

	public String getCollectionURI(final int index) {
		return ((collectionURIs != null) && ((index >= 0) && (index < collectionURIs.size()))) ? collectionURIs.get(
				index).toString() : "";
	}

	/**
	 * Get the value of a custom field.
	 * @param key
	 * @return
	 */
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

	/**
	 * Sort by the given field
	 * 
	 * @param fieldName
	 *            the field
	 * @param ascending
	 *            1 for ascending, 0 for descending
	 */
	public String sortBy(final String fieldName, final int ascending) {
		final Document document = getDocument();
		final NodeList nodes = document.getElementsByTagName("item");

		final ArrayList<ItemHolder> items = new ArrayList<ItemHolder>();
		final ArrayList<VFSPath> missingURIs = new ArrayList<VFSPath>();

		for (int i = 0; i < nodes.getLength(); i++) {
			final Node current = nodes.item(i);
			if (current.getNodeName().equalsIgnoreCase("item")) {
				final VFSPath itemURI;
				try {
					itemURI = new VFSPath(DocumentUtils.impl.getAttribute(current, "uri"));
				} catch (IllegalArgumentException e) {
					GoGoEgo.debug("debug").println("Error: Could not parse uri " + DocumentUtils.impl.getAttribute(current, "uri"));
					continue;
				}
				
				try {
					final GoGoEgoItemRepresentation item = (GoGoEgoItemRepresentation) builder.handleGet(request, itemURI);
					items.add(new ItemHolder(itemURI, item.resolveEL(fieldName)));
				} catch (final Exception e) {
					missingURIs.add(itemURI);
				}
			}
		}

		if (ascending == 1)
			Collections.sort(items);
		else
			Collections.sort(items, Collections.reverseOrder());

		itemURIs = new ArrayList<VFSPath>();
		for (int i = 0; i < items.size(); i++)
			itemURIs.add(items.get(i).url);

		itemURIs.addAll(missingURIs);

		return "";
	}

	public String sortCollectionsBy(final String fieldName, final int ascending) {
		final Document document = getDocument();
		final NodeList nodes = document.getElementsByTagName("subcollection");

		final ArrayList<CollectionHolder> items = new ArrayList<CollectionHolder>();
		final ArrayList<VFSPath> missingURIs = new ArrayList<VFSPath>();

		for (int i = 0; i < nodes.getLength(); i++) {
			final Node current = nodes.item(i);
			if (current.getNodeName().equals("subcollection")) {
				final VFSPath collectionURI;
				try {
					collectionURI = new VFSPath(DocumentUtils.impl.getAttribute(current, "uri"));
				} catch (IllegalArgumentException e) {
					GoGoEgo.debug("debug").println("Error; Could not parse collection "
							+ DocumentUtils.impl.getAttribute(current, "uri"));
					continue;
				}
				final Request newRequest = new InternalRequest(request, Method.GET, "riap://host" + collectionURI);
				final Response response = context.getClientDispatcher().handle(newRequest);

				try {
					GoGoEgoCollectionRepresentation collection = (GoGoEgoCollectionRepresentation) response.getEntity();
					
					items.add(new CollectionHolder(collectionURI, collection.resolveEL(fieldName)));
				} catch (Exception e) {
					missingURIs.add(collectionURI);
				}
			}
		}

		if (ascending == 1)
			Collections.sort(items);
		else
			Collections.sort(items, Collections.reverseOrder());

		collectionURIs = new ArrayList<VFSPath>();
		for (int i = 0; i < items.size(); i++)
			collectionURIs.add(items.get(i).url);

		collectionURIs.addAll(missingURIs);

		return "";
	}

	protected ArrayList<VFSPath> getItemURIs() {
		if (itemURIs == null) {
			itemURIs = new ArrayList<VFSPath>();
			final TagFilter tf = new TagFilter(new StringReader(getContent()));
			tf.shortCircuitClosingTags = true;
			tf.registerListener(new BaseTagListener() {
				public void process(Tag t) throws IOException {
					try {
						itemURIs.add(new VFSPath(t.getAttribute("uri")));
					} catch (IllegalArgumentException e) {
						GoGoEgo.debug("debug").println("Error parsing item {0}", t.getAttribute("uri"));
					}
				}				
				public List<String> interestingTagNames() {
					final List<String> list = new ArrayList<String>();
					list.add("item");
					return list;
				}
			});
			try {
				tf.parse();
			} catch (IOException impossible) {
				TrivialExceptionHandler.impossible(this, impossible);
			}
		}
		return itemURIs;
	}

	protected ArrayList<VFSPath> getCollectionURIs() {
		if (collectionURIs == null) {
			collectionURIs = new ArrayList<VFSPath>();
			final TagFilter tf = new TagFilter(new StringReader(getContent()));
			tf.shortCircuitClosingTags = true;
			tf.registerListener(new BaseTagListener() {
				public void process(Tag t) throws IOException {
					try {
						collectionURIs.add(new VFSPath(t.getAttribute("uri")));
					} catch (IllegalArgumentException e) {
						GoGoEgo.debug("debug").println("Error parsing item {0}", t.getAttribute("uri"));
					}
				}				
				public List<String> interestingTagNames() {
					final List<String> list = new ArrayList<String>();
					list.add("subcollection");
					return list;
				}
			});
			try {
				tf.parse();
			} catch (IOException impossible) {
				TrivialExceptionHandler.impossible(this, impossible);
			}
		}
		return collectionURIs;
	}
	
	public String resolveEL(String key) {
		if (key.equals("id") || key.equals("collectionID"))
			return getCollectionID();
		else if (key.equals("name") || key.equals("collectionName"))
			return getCollectionName();
		
		String value = getValue(key);
		if ("description".equals(key) && !cache.containsKey(key))
			return getDescription();
		else if ("keywords".equals(key) && !cache.containsKey(key))
			return getKeywords();
		
		if (cache.containsKey(key))
			return value;
		else
			return super.resolveEL(key);
	}
	
	public String getURI() {
		return request.getResourceRef().getPath();
	}
	
	public Trap<GoGoEgoCollectionRepresentation> newTrap() {
		return new GoGoEgoCollectionRepresentationTrap(this);
	}
	
	private static class CollectionHolder implements Comparable<CollectionHolder> {
		private final String field;
		private final VFSPath url;

		public CollectionHolder(final VFSPath url, final String fieldValue) {
			this.url = url;
			field = fieldValue;
		}

		public int compareTo(final CollectionHolder other) {
			return field.equals("") ? -1 : comparator.compare(field, other.field);
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof CollectionHolder)
				return compareTo((CollectionHolder) other) == 0;
			else
				return false;
		}

		@Override
		public int hashCode() {
			return (field + url).hashCode();
		}
	}

	private static class ItemHolder implements Comparable<ItemHolder> {
		private final String field;
		private final VFSPath url;

		public ItemHolder(final VFSPath url, final String fieldValue) {
			this.url = url;
			field = fieldValue;
		}

		public int compareTo(final ItemHolder other) {
			Date d1 = convertToDate(field);
			Date d2 = convertToDate(other.field);
			if (d1 == null || d2 == null)
				return field.equalsIgnoreCase("") ? -1 : comparator.compare(field, other.field);
			else
				return d1.compareTo(d2);
		}

		public Date convertToDate(String field) {
			if (field.indexOf("/") == -1)
				return null;
			
			final String[] fullSplit = field.split(",");
			
			/*
			 * First attempt to convert the date portion...
			 */
			String[] split = fullSplit[0].split("/");
			if (split.length != 3)
				return null;

			int day, month, year;
			try {
				day = Integer.parseInt(split[1]);
				month = Integer.parseInt(split[0]);
				year = Integer.parseInt(split[2]);
			} catch (NumberFormatException e) {
				return null;
			}

			Calendar c = Calendar.getInstance(Locale.getDefault());
			c.set(year - 1900, month - 1, day);
			
			if (fullSplit.length == 1 || fullSplit[1].length() != 7)
				return c.getTime();
			
			/*
			 * ...then attempt to convert the time portion
			 */
			String timeStr = fullSplit[1];
			int hour, minute;
			try {
				char[] hourArr = new char[2];
				hourArr[0] = timeStr.charAt(0);
				hourArr[1] = timeStr.charAt(1);
				hour = Integer.parseInt(new String(hourArr));
				
				hour = hour - 1;
				if ('P' == timeStr.charAt(5))
					hour += 12;
				
				char[] minuteArr = new char[2];
				minuteArr[0] = timeStr.charAt(3);
				minuteArr[1] = timeStr.charAt(4);
				minute = Integer.parseInt(new String(minuteArr));
			} catch (Exception e) {
				return c.getTime();
			}
			
			c.set(year - 1900, month - 1, day, hour, minute);
			
			return c.getTime();
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof ItemHolder)
				return compareTo((ItemHolder) other) == 0;
			else
				return false;
		}

		@Override
		public int hashCode() {
			return (field + url).hashCode();
		}
	}

}
