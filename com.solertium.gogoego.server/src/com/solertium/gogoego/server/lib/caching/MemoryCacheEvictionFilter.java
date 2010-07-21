/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
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

import java.util.ArrayList;
import java.util.Collection;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.filters.ConditionalGetProcess;
import com.solertium.util.TrivialExceptionHandler;

/**
 * MemoryCacheEvictionFilter.java
 * 
 * Filter that attempts to evict based on path when a
 * write method is successfully performed.
 * 
 * @author carl.scott
 *
 */
public class MemoryCacheEvictionFilter extends Filter {
	
	protected final Collection<Method> writeMethods;
	
	MemoryCacheEvictionFilter(Context context) {
		this(context, null);
	}
	
	MemoryCacheEvictionFilter(Context context, Restlet next) {
		super(context, next);
		writeMethods = new ArrayList<Method>();
		writeMethods.add(Method.PUT);
		writeMethods.add(Method.POST);
		writeMethods.add(Method.MOVE);
		writeMethods.add(Method.COPY);
		writeMethods.add(Method.DELETE);
	}
	
	protected void afterHandle(Request request, Response response) {
		if (writeMethods.contains(request.getMethod()) && response.getStatus().isSuccess()) {
			final String path = request.getResourceRef().getPath();
			String invalidated = null;
			try {
				invalidated = MemoryCache.getInstance().getLandlord(getContext()).invalidate(getContext(), path);
			} catch (Throwable e) { //Anything can happen with OSGi plugins
				TrivialExceptionHandler.ignore(this, e);
			}
			
			try {
				new Thread(new ConditionalGetProcess(invalidated, ServerApplication.getFromContext(getContext()))).start();
			} catch (Throwable e) {
				//Its not that important
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}

}
