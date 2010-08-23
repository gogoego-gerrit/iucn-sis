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
package com.solertium.gogoego.server.lib.caching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gogoego.api.collections.CollectionCache;
import org.gogoego.api.collections.CollectionCacheContent;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.settings.DocumentSimpleSettingsReader;
import com.solertium.gogoego.server.lib.settings.SimpleSettingsReader;
import com.solertium.gogoego.server.lib.settings.cache.CacheSettingsLoader;
import com.solertium.util.AlphanumericComparator;
import com.solertium.util.BaseDocumentUtils;

/**
 * CacheDumpResource.java
 * 
 * Provides a dump and means of clearing the cache. 
 * Use /admin/cache for all caches, or use a protocol 
 * to operate on a particular cache. 
 * 
 * @author carl.scott
 *
 */
public class CacheDumpResource extends Resource {
	
	private static final String CACHE_PROTOCOL_COLLECTIONS = "collections";
	private static final String CACHE_PROTOCOL_MEMORY = "memory";
	
	private final String siteID;
	private final String protocol;

	public CacheDumpResource(Context context, Request request, Response response) {
		super(context, request, response);
		siteID = GoGoEgo.get().getFromContext(getContext()).getSiteID();
		protocol = request.getResourceRef().getLastSegment();
		setModifiable(true);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		if ("settings".equals(protocol)) {
			Document document = null;
			try {
				document = GoGoEgo.get().getFromContext(getContext()).
					getVFS().getDocument(CacheSettingsLoader.SETTINGS_PATH);
			} catch (IOException e) {
				document = null;
			}
			
			if (document == null) {
				document = BaseDocumentUtils.impl.newDocument();
				document.appendChild(document.createElement("root"));
			}
			
			return new GoGoEgoDomRepresentation(document);
		}
		
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("div");
	
		if (CACHE_PROTOCOL_COLLECTIONS.equals(protocol))
			appendCollectionCache(document, root);
		else if (CACHE_PROTOCOL_MEMORY.equals(protocol))
			appendMemoryCache(document, root);
		else {
			appendCollectionCache(document, root);
			appendMemoryCache(document, root);
		}
		
		document.appendChild(root);
		
		return new DomRepresentation(variant.getMediaType(), document);
	}
	
	private void appendCollectionCache(Document document, Element root) {
		Map<String, CollectionCacheContent> map = 
			CollectionCache.getInstance().getCache(getContext());
		List<String> nodes = new ArrayList<String>(map.keySet());
		
		final SimpleSettingsReader reader = 
			ServerApplication.getFromContext(getContext()).getSettingsStorage().get("cache");
		
		boolean enabled = reader == null || !"false".equals(reader.getField("collections_enabled"));
		
		final Element h1 = 
			BaseDocumentUtils.impl.createElementWithText(document, "h1", "Collection Cache Contents");
		h1.setAttribute("enabled", Boolean.toString(enabled));
		
		root.appendChild(h1);
		if (nodes.isEmpty())
			root.appendChild(BaseDocumentUtils.impl.createElementWithText(document, "p", "Cache empty."));
		else {
			Collections.sort(nodes, new AlphanumericComparator());
			final Element parent = document.createElement("ul");
			for (String uri : nodes) {
				final Element node = 
					BaseDocumentUtils.impl.createElementWithText(document, "li", uri);
				node.setAttribute("time", Long.toString(map.get(uri).getTimeCached().getTime()));
				parent.appendChild(node);
			}
			root.appendChild(parent);
		}
	}
	
	private void appendMemoryCache(Document document, Element root) {
		if (MemoryCache.getInstance().isEnabled()) {
			final Map<String, MemoryCacheContents> map = new HashMap<String, MemoryCacheContents>();
			for (Map.Entry<String, MemoryCacheContents> entry : MemoryCache.getInstance().getCache().entrySet()) {
				final String[] split = entry.getKey().split(":");
				if (split[0].equals(siteID))
					map.put(split[1], entry.getValue());
			}
			
			final List<String> mNodes = new ArrayList<String>(map.keySet());
			
			final SimpleSettingsReader reader = 
				ServerApplication.getFromContext(getContext()).getSettingsStorage().get("cache");
			
			boolean enabled = reader == null || !"false".equals(reader.getField("memory_enabled"));
			
			final Element h1 = 
				BaseDocumentUtils.impl.createElementWithText(document, "h1", "Memory Cache Contents");
			h1.setAttribute("enabled", Boolean.toString(enabled));
			
			root.appendChild(h1);
			if (mNodes.isEmpty())
				root.appendChild(BaseDocumentUtils.impl.createElementWithText(document, "p", "Cache empty."));
			else {
				Collections.sort(mNodes);
				final Element parent = document.createElement("ul");
				for (String uri : mNodes) {
					final Element node = 
						BaseDocumentUtils.impl.createElementWithText(document, "li", uri);
					node.setAttribute("time", Long.toString(map.get(uri).getTimeCached().getTime()));
					node.setAttribute("expires", Long.toString(map.get(uri).getExpirationDate().getTime()));
					parent.appendChild(node);
				}
				root.appendChild(parent);
			}			
		}
	}
	
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final SimpleSettingsReader newSettings = 
			new DocumentSimpleSettingsReader(document);
		
		final SimpleSettingsReader storedSettings = 
			ServerApplication.getFromContext(getContext()).getSettingsStorage().get("cache");
		
		final Document settings = BaseDocumentUtils.impl.newDocument();
		final Element root = settings.createElement("root");
	
		final Element memField = settings.createElement("field");
		memField.setAttribute("name", "memory_enabled");
		memField.setTextContent(newSettings.getField("memory_enabled", 
			storedSettings.getField("memory_enabled", "true")));
		
		root.appendChild(memField);
		
		final Element colField = settings.createElement("field");
		colField.setAttribute("name", "collections_enabled");
		colField.setTextContent(newSettings.getField("collections_enabled", 
			storedSettings.getField("collections_enabled", "true")));
		
		root.appendChild(colField);
		
		settings.appendChild(root);
		
		if (DocumentUtils.writeVFSFile(CacheSettingsLoader.SETTINGS_PATH.toString(), 
				GoGoEgo.get().getFromContext(getContext()).getVFS(), settings)) {
			ServerApplication.getFromContext(getContext()).getSettingsStorage().flush("cache");			
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		}
		else
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	}
	
	public void removeRepresentations() throws ResourceException {
		if (CACHE_PROTOCOL_COLLECTIONS.equals(protocol))
			CollectionCache.getInstance().invalidateInstance(getContext());
		else if (CACHE_PROTOCOL_MEMORY.equals(protocol)) {
			MemoryCache.getInstance().clear(getContext());
			MemoryCache.getInstance().getLandlord(getContext()).invalidateAll(getContext());
		}
		else {
			CollectionCache.getInstance().invalidateInstance(getContext());
			MemoryCache.getInstance().clear(getContext());
			MemoryCache.getInstance().getLandlord(getContext()).invalidateAll(getContext());
		}			
	}

}
