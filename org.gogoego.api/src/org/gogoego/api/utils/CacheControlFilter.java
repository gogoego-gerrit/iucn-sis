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
package org.gogoego.api.utils;

import org.gogoego.api.collections.Constants;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

import com.solertium.util.restlet.RestletUtils;

/**
 * CacheControlFilter.java
 * 
 * Filter that sets cache-control information for all 
 * resources accessed behind it.
 * 
 * @author carl.scott
 * 
 */
public class CacheControlFilter extends Filter {

	private String cachingMethod;

	public CacheControlFilter(final Context context, final String cachingMethod) {
		super(context);
		this.cachingMethod = cachingMethod;
	}

	protected void afterHandle(Request request, Response response) {
		RestletUtils.addHeaders(response, "Cache-control", cachingMethod);
		response.getAttributes().put(Constants.DISABLE_EXPIRATION, Boolean.TRUE);
	}

}
