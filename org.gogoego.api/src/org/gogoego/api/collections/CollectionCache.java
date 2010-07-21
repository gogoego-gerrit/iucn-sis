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
import java.util.Map;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoCollectionRepresentation;
import org.gogoego.api.representations.GoGoEgoItemRepresentation;
import org.restlet.Context;
import org.restlet.data.Request;

import com.solertium.util.BoundedHashMap;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFSPath;

/**
 * CollectionCache.java
 * 
 * A single instance of a BoundedHashMap, sized to a cache size 
 * specified in component properties.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionCache {
	
	private static final String DEFAULT_SIZE = "1024";	
	private static CollectionCache impl;
	
	public static CollectionCache getInstance() {
		if (impl == null)
			impl = new CollectionCache();
		return impl;
	}
	
	private final BoundedHashMap<String, CollectionCacheContent> cache;
	private final boolean useFullEvictionPolicy;
	
	private CollectionCache() {
		int size;
		try {
			size = Integer.parseInt(GoGoEgo.getInitProperties().getProperty(
				Constants.PROPERTY_COLLECTION_CACHE_SIZE, DEFAULT_SIZE));
		} catch (NumberFormatException e) {
			size = Integer.parseInt(DEFAULT_SIZE);
		}
		
		cache = new BoundedHashMap<String, CollectionCacheContent>(size);
		
		useFullEvictionPolicy = "full".equalsIgnoreCase(GoGoEgo.getInitProperties().getProperty(Constants.PROPERTY_COLLECTION_CACHE_EVICTION));
	}
	
	public Map<String, CollectionCacheContent> getCache(Context context) {
		final String siteID = GoGoEgo.get().getFromContext(context).getSiteID();
		final Map<String, CollectionCacheContent> map = new HashMap<String, CollectionCacheContent>();
		for (Map.Entry<String, CollectionCacheContent> entry : cache.entrySet()) {
			final String[] split = entry.getKey().split(":");
			if (split[0].equals(siteID))
				map.put(split[1], entry.getValue());
		}
		return map;
	}
	
	public void cache(Context context, String path, CollectionCacheContent cacheContent) {
		GoGoEgo.debug().println("Collection Cache Caching {0}", path);
		cache.put(GoGoEgo.get().getFromContext(context).getSiteID() + ":" + path, cacheContent);
	}
	
	public boolean isCached(Context context, String path) {
		return cache.containsKey(GoGoEgo.get().getFromContext(context).getSiteID() + ":" + path);
	}
	
	public void invalidate(Context context, String path) {
		GoGoEgo.debug().println("Collection Cache Invalidating {0}", path);
		if (useFullEvictionPolicy)
			cache.clear();
		else {
			try {
				cache.remove(GoGoEgo.get().getFromContext(context).getSiteID() + ":" + path);
			} catch (NullPointerException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}
	
	public void invalidateInstance(Context context) {
		final String siteID = GoGoEgo.get().getFromContext(context).getSiteID();
		for (String key : cache.keySet())
			if (key.startsWith(siteID))
				cache.remove(key);
	}
	
	public CollectionCacheContent getCacheContent(Context context, String path) {
		return cache.get(GoGoEgo.get().getFromContext(context).getSiteID() + ":" + path);
	}
	
	public GoGoEgoBaseRepresentation getRepresentation(Context context, Request request, String path) {
		if (!isCached(context, path))
			return null;
		
		final String siteID = GoGoEgo.get().getFromContext(context).getSiteID();
		
		final CollectionCacheContent cacheContent = cache.get(siteID + ":" + path);		
		
		final GoGoEgoBaseRepresentation representation;
		if (!cacheContent.hasItem()) {
			representation = new GoGoEgoCollectionRepresentation(request, context, cacheContent.getCategoryData().getCategory().toXML());
			/*
			 * TODO: not sure how well this will hold up, probably want to 
			 * either cache each variant separately or detect the variant 
			 * and assign it appropriately... 
			 */
			representation.setMediaType(cacheContent.getMediaType());
		}
		else {
			representation = new GoGoEgoItemRepresentation(request, context, 
				new VFSPath("/(SYSTEM)" + cacheContent.getCategoryData().getCategory().getCollectionAccessURI() + "/" + Constants.COLLECTION_ROOT_FILENAME), cacheContent.getItem().toXML());
			representation.setMediaType(cacheContent.getMediaType());
		}
		try {
			representation.setSize(cacheContent.getSize());
			representation.setModificationDate(cacheContent.getModificationDate());
			representation.setTag(cacheContent.getTag());
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(representation, e);
		}
		
		return representation;
	}

}
