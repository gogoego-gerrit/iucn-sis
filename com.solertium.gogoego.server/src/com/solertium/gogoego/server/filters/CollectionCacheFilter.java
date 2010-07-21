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
package com.solertium.gogoego.server.filters;

import java.util.Calendar;
import java.util.Date;

import org.gogoego.api.collections.CollectionCache;
import org.gogoego.api.collections.CollectionCacheContent;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoCollectionRepresentation;
import org.gogoego.api.representations.GoGoEgoItemRepresentation;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.settings.SimpleSettingsReader;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * CollectionCacheFilter.java
 * 
 * This cache filter is meant to be used on the public resource. 
 * It uses the global cache to cache data based on the site and 
 * URI.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionCacheFilter extends Filter {

	public CollectionCacheFilter(Context context) {
		super(context);
	}
	
	public CollectionCacheFilter(Context context, Restlet next) {
		super(context, next);
	}
	
	protected int beforeHandle(Request request, Response response) {
		if (!Method.GET.equals(request.getMethod()))
			return Filter.CONTINUE;
		
		//This is a special request and should be handled elsewhere
		if (request.getResourceRef().getQuery() != null)
			return Filter.CONTINUE;
		
		final SimpleSettingsReader reader = 
			ServerApplication.getFromContext(getContext()).getSettingsStorage().get("cache");
		
		//Client has disabled caching
		if (reader != null && "false".equals(reader.getField("collections_enabled")))
			return Filter.CONTINUE;
		
		final String path;
		try {
			path = VFSResource.decodeVFSPath(request.getResourceRef().getPath()).toString();
		} catch (VFSUtils.VFSPathParseException e) {
			return Filter.CONTINUE;
		}
		
		if (CollectionCache.getInstance().isCached(getContext(), path)) {
			log("Cache hit");
			
			final GoGoEgoBaseRepresentation representation = getRepresentation(getContext(), path, request, response);
			
			response.setEntity(representation);
			
			return Filter.STOP;
		}
		else {
			request.getAttributes().put(Constants.COLLECTION_CACHE_CONTENT, Calendar.getInstance().getTime());
			return Filter.CONTINUE;
		}
	}
	
	protected static GoGoEgoBaseRepresentation getRepresentation(Context context, String path, Request request, Response response) {
		final CollectionCacheContent cacheContent = CollectionCache.getInstance().getCacheContent(context, path);		
		
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
		
		try {
			response.getAttributes().putAll(cacheContent.getResponseAttributes());
		} catch (NullPointerException ignored) {
			TrivialExceptionHandler.ignore(response, ignored);
		}
		
		return representation;
	}
	
	protected void afterHandle(Request request, Response response) {
		if (!Method.GET.equals(request.getMethod()) || !response.getStatus().isSuccess() || !response.getAttributes().containsKey(Constants.COLLECTION_CACHE_CONTENT))				
			return;
		
		final CollectionCacheContent cacheContent = (CollectionCacheContent)
			response.getAttributes().get(Constants.COLLECTION_CACHE_CONTENT);
		
		final Representation entity = response.getEntity();
		
		cacheContent.setMediaType(entity.getMediaType());
		cacheContent.setModificationDate(entity.getModificationDate());
		cacheContent.setResponseAttributes(response.getAttributes());
		cacheContent.setSize(entity.getSize());
		cacheContent.setTag(entity.getTag());
		
		final String path;
		try {
			path = VFSResource.decodeVFSPath(request.getResourceRef().getPath()).toString();
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.impossible(this, e);
			return;
		}
		
		CollectionCache.getInstance().cache(getContext(), path, cacheContent);
		
		response.setEntity(entity);
		
		Date start = (Date)request.getAttributes().get(Constants.COLLECTION_CACHE_CONTENT);
		Date end = Calendar.getInstance().getTime();
		
		log("Fetched, cached and returned collection representation in {0} ms", (end.getTime() - start.getTime()));
	}
	
	private void log(String out) {
		//GoGoDebug.system().println(out);
	}
	
	private void log(String out, Object... objs) {
		//GoGoDebug.system().println(out, objs);
	}

}