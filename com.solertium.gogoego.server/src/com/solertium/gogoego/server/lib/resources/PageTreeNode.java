/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.lib.resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.lib.caching.MemoryCacheContents;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;

/**
 * PageTreeNode.java
 * 
 * A node in the page tree.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class PageTreeNode {
	
	private static DateFormat format = SimpleDateFormat.getDateTimeInstance();
	
	private final Collection<PageTreeNode> children;
	private final String uri;
	private String contentType, tag, base;
	private final long lastModified;
	
	private Map<String, String> modifiers;
	private String accessURI;
	private int level;
	
	public PageTreeNode(String uri, String contentType, String tag, long lastModified) {
		this.uri = uri;
		this.contentType = contentType;
		this.tag = tag;
		this.lastModified = lastModified;
		this.level = 0;
		children = new ArrayList<PageTreeNode>();
	}

	public PageTreeNode(String uri, String contentType, String tag, long lastModified, String base) {
		this(uri, contentType, tag, lastModified);
		this.base = base;
	}
	
	public boolean contains(String uri) {
		boolean found = getUri().equals(uri);
		for (Iterator<PageTreeNode> iter = children.iterator(); iter.hasNext() && !(found |= iter.next().contains(uri)); );
		return found;
	}
	
	public long getRawLastModified() {
		return lastModified;
	}
	
	public long getLastModified() {
		long highest = lastModified == 0 ? new Date().getTime() : lastModified; 
		
		for (PageTreeNode node : children) {
			long potential = node.getLastModified();
			if (potential > highest)
				highest = potential;
		}
		
		return highest;
	}
	
	public Date getExpirationDate() {
		Date bar;
		if (modifiers == null || modifiers.get("cache-expires") == null)
			bar = null;
		else
			bar = MemoryCacheContents.parseExpirationDate(modifiers.get("cache-expires"));
		
		for (PageTreeNode node : children) {
			Date potential = node.getExpirationDate();
			if (potential != null && (bar == null || potential.before(bar)))
				bar = potential;
		}
		
		return bar;
	}
	
	public void setAccessURI(String accessURI) {
		this.accessURI = accessURI;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setModifiers(Map<String, String> modifiers) {
		this.modifiers = modifiers;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
		
	public void addChild(PageTreeNode child) {
		if (children.contains(child))
			return;
		
		int newLevel = child.getLevel() + 1;
		if (getLevel() < newLevel)
			setLevel(newLevel);
		children.add(child);		
	}
	
	public Set<String> getAllUris() {
		final HashSet<String> list = new HashSet<String>();
		if (getUri().startsWith("/"))
			list.add(getUri());
		
		for (PageTreeNode child : children)
			list.addAll(child.getAllUris());
		
		return list;
	}
	
	public Map<String, String> getModifiers() {
		return modifiers;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getTag() {
		return tag;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public boolean hasContent() {
		boolean found = false;
		for (Iterator<PageTreeNode> iter = children.iterator(); (iter.hasNext() && !(found = iter.next().isContent())); );
		return found;
		/*boolean found = false;
		for (Iterator<PageTreeNode> iter = children.iterator(); (iter.hasNext() && !found); ) {
			PageTreeNode curChild = iter.next();
			found = curChild.isContent() || curChild.hasContent();
		}
		return found;*/
	}
	
	public boolean isContent() {
		return "content".equals(tag);
	}
	
	public boolean isTemplateWithContent() {
		return hasContent() && "template".equals(tag);
	}
	
	public PageTreeNode getContentForURI(String uri) {
		PageTreeNode node = null;
		for (Iterator<PageTreeNode> iter = children.iterator(); iter.hasNext() && node == null;) {
			PageTreeNode current = iter.next();
			if (uri.startsWith(current.uri))
				node = current;
		}
		return node;
	}
		
	public String toString() {
		return BaseDocumentUtils.impl.serializeDocumentToString(toXML(), true, true);
	}
	
	public Document toXML() {
		Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("root");
		root.appendChild(createNode(document));
		document.appendChild(root);
		return document;
	}
	
	private Node createNode(Document document) {
		Element node = document.createElement(tag);
		node.setAttribute("uri", uri);
		node.setAttribute("contentType", contentType);
		node.setAttribute("level", Integer.toString(getLevel()));
		if (lastModified != 0) {
			try {
				node.setAttribute("lastModified", format.format(new Date(lastModified)));
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		node.setAttribute("finalLastModified", format.format(new Date(getLastModified())));
		if (modifiers != null && !modifiers.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			for (Iterator<Map.Entry<String, String>> iterator = modifiers.entrySet().iterator(); iterator.hasNext(); ) {
				final Map.Entry<String, String> entry = iterator.next();
				sb.append(entry.getKey());
				if (entry.getValue() != null) {
					sb.append('=');
					sb.append(entry.getValue());
				}
				if (iterator.hasNext())
					sb.append(';');
			}
			node.setAttribute("modifiers", sb.toString());
		}
		if (base != null)
			node.setAttribute("base", base);
		if (accessURI != null && !accessURI.equals(uri))
			node.setAttribute("accessURI", accessURI);
		
		/*if (base != null)
			node.appendChild(base.createNode(document, "base"));*/
		for (PageTreeNode child : children)
			node.appendChild(child.createNode(document));
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessURI == null) ? 0 : accessURI.hashCode());
		result = prime * result
				+ ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		PageTreeNode other = (PageTreeNode) obj;
		if (accessURI == null) {
			if (other.accessURI != null)
				return false;
		} else if (!accessURI.equals(other.accessURI))
			return false;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (lastModified != other.lastModified)
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
