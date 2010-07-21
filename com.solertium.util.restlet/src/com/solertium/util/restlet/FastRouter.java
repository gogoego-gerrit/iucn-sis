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
package com.solertium.util.restlet;

import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

import com.solertium.util.BoundedHashMap;

/**
 * Route scoring and scanning can be expensive, especially if there
 * are many routes in a given Router, as all must be checked.  This
 * version of Router specially handles the common case of wiring a
 * Restlet or Resource to a fixed location in the URI space with no
 * template processing.  This is detected on attach when no curly
 * braces are found in the attach point, and is called a "mount" here.
 * 
 * Mounts are checked quickly using a fast scan, and dispatched before
 * normal Routes.  However, the usual Route/Finder pattern is used so
 * that downstream behavior is compatible.  There is a small FIFO
 * cache that captures and reuses exact matches, bypassing the scan
 * altogether.
 * 
 * To use this class, just say new FastRouter() where you would
 * otherwise say new Router().
 * 
 * Mounted routes also bypass the logRoute call.
 * 
 * @author robheittman
 */
public class FastRouter extends Router {
	
	public FastRouter(Context context){
		super(context);
	}
	
	private static final int CACHE_SIZE = 256;
	
	private final ConcurrentHashMap<String,Restlet> mounts = new ConcurrentHashMap<String,Restlet>();

	private final BoundedHashMap<String,Restlet> remembered = new BoundedHashMap<String,Restlet>(CACHE_SIZE);
	
	private void mount(String location, Restlet target){
		mounts.put(location, target);
	}

	@Override
	@SuppressWarnings("deprecation")
	public Route attach(String pathTemplate, Restlet target) {
		remembered.clear();
		if(!"".equals(pathTemplate)
				&& pathTemplate.indexOf("{") == -1){
	        final Route result = createRoute(pathTemplate, target);
	        mount(pathTemplate, result);
	        return result;
		}
		return super.attach(pathTemplate, target);
    }
	
	@Override
	public Restlet getNext(Request request, Response response) {
		String uri = request.getResourceRef().getRemainingPart();
		Restlet known = remembered.get(uri);
		if(known!=null){
			return known;
		}
		int bestLength = 0;
		String best = null;
		if(uri!=null){
			final int l = uri.length();
			if(l>0){
				for(String s : mounts.keySet()){
					final int ml = s.length();
					if(l>=ml && uri.startsWith(s) && ml>bestLength){
						bestLength = ml;
						best = s;
					}
				}
			}
		}
		final Restlet result;
		if(best!=null){
			result = mounts.get(best);
		} else {
			result = super.getNext(request, response);
		}
		remembered.put(uri, result);
		return result;
    }
	
}
