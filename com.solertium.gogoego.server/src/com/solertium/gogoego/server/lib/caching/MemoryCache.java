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
package com.solertium.gogoego.server.lib.caching;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.resources.PageTreeNode;
import com.solertium.gogoego.server.lib.settings.SimpleSettingsReader;
import com.solertium.util.BoundedHashMap;

/**
 * MemoryCache.java
 * 
 * Prototype for the memory cache behavior.  Still has the following items TODO:
 * 
 * - Eviction policy based on the cached content size
 * - Read cache policy from properties file
 * - Invalidate on write operations
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class MemoryCache {
	
	private static final String DEFAULT_SIZE = "1024";
	
	private static MemoryCache instance;
	
	public static MemoryCache getInstance() {
		if (instance == null)
			instance = new MemoryCache();
		return instance;
	}
	
	private final BoundedHashMap<String, MemoryCacheContents> cache;
	private final boolean isEnabled;
	private final Map<String, MemoryCacheLandlord> landlordMap;
	
	private MemoryCache() {
		int size;
		try {
			size = Integer.parseInt(GoGoEgo.getInitProperties().getProperty(
				Constants.PROPERTY_MEMORY_CACHE_SIZE, DEFAULT_SIZE	
			));
		} catch (NumberFormatException e) {
			size = Integer.parseInt(DEFAULT_SIZE);
		}
		
		cache = new BoundedHashMap<String, MemoryCacheContents>(size);
		
		landlordMap = new HashMap<String, MemoryCacheLandlord>();
		
		isEnabled = "true".equals(GoGoEgo.getInitProperties().getProperty(Constants.PROPERTY_MEMORY_CACHE_ENABLED, "true"));
	}
	
	public void cache(Context context, String path, String content, String expires) {
		cache(context, path, content, expires, null);
	}
	
	public void cache(Context context, String path, String content, String expires, PageTreeNode pageTree) {
		final Set<String> dependents;
		if (pageTree == null)
			dependents = new HashSet<String>(Arrays.asList(path));
		else
			dependents = pageTree.getAllUris();
		
		if (isEnabled(context) && getLandlord(context).canCache(path)) {
			cache.put(getUri(context, path), new MemoryCacheContents(content, expires, pageTree));
			//dependents.add(path);
			getLandlord(context).cache(path, dependents);
		}
	}
	
	public void clear() {
		cache.clear();
	}
	
	public void clear(Context context) {
		final String siteID = GoGoEgo.get().getFromContext(context).getSiteID();
		for (String uri : cache.keySet())
			if (uri.startsWith(siteID))
				cache.remove(uri);
	}
	
	public boolean contains(Context context, String path) {
		MemoryCacheContents contents;
		try {
			contents = cache.get(getUri(context, path));
		} catch (Throwable e) {
			return false;
		}
		if (contents == null)
			return false;
		if (contents.isExpired()) {
			cache.remove(getUri(context, path));
			return false;
		}
		return isEnabled(context);
	}

	public MemoryCacheContents get(final Context context, final String path) {
		return contains(context, path) ? cache.get(getUri(context, path)) : null;
	}
	
	public MemoryCacheLandlord getLandlord(final Context context) {
		String siteID = GoGoEgo.get().getFromContext(context).getSiteID();
		if (!landlordMap.containsKey(siteID))
			landlordMap.put(siteID, new MemoryCacheLandlord());
		return landlordMap.get(siteID);
	}
	
	public BoundedHashMap<String, MemoryCacheContents> getCache() {
		return cache;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public boolean isEnabled(Context context) {
		SimpleSettingsReader reader = 
			ServerApplication.getFromContext(context).getSettingsStorage().get("cache");
		return isEnabled && (reader == null || !"false".equals(reader.getField("memory_enabled")));
	}
	
	public void invalidate(final Context context, String path) {
		cache.remove(getUri(context, path));
	}
	
	private String getUri(Context context, String path) {
		return GoGoEgo.get().getFromContext(context).getSiteID() + ":" + path;
	}	

}
