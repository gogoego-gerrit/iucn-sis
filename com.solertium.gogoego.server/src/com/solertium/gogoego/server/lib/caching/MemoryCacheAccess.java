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

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

import com.solertium.util.SysDebugger;

/**
 * MemoryCacheAccess.java
 * 
 * Define what is and is not cacheable.
 * 
 * @author carl.scott
 *
 */
public class MemoryCacheAccess {
	
	private static final int ACCESS_MODE_ALLOW = 0;
	private static final int ACCESS_MODE_DENY = 1;
	
	private final Router router;
	
	public static void main(String[] args) {
		MemoryCacheAccess x = new MemoryCacheAccess();
		x.allowPath("/collections");
		x.allowPath("/apps/cacheable");
		x.denyPath("/apps");
		
		
		final String[] tests = new String[] {
			"/collections", "/collections/blah", 
			"/apps", "/apps/blah", "/apps/cacheable", 
			"/includes/file.html"			
		};
		
		for (String test : tests)
			SysDebugger.out.println("{0} -> {1}", test, x.canCache(test));
	}
	
	public MemoryCacheAccess() {
		router = new Router();
		router.attachDefault(new PlaceHolder(ACCESS_MODE_ALLOW));
		denyPath("/apps");
	}
	
	public void allowPath(String uri) {
		addPath(uri, ACCESS_MODE_ALLOW);
	}
	
	public void denyPath(String uri) {
		addPath(uri, ACCESS_MODE_DENY);
	}
	
	private void addPath(String uri, int accessMode) {
		router.attach(uri, new PlaceHolder(accessMode));
	}
	
	public boolean canCache(String uri) {
		final Request fauxRequest = new Request(Method.GET, uri);
		final Response fauxResponse = new Response(fauxRequest);
		
		final Restlet next = router.getNext(fauxRequest, fauxResponse);
		if (next == null || !(next instanceof Route))
			return false;
		
		Route route = (Route)next;
		
		PlaceHolder placeHolder = (PlaceHolder)route.getNext();
		
		return placeHolder.getAccessMode() == ACCESS_MODE_ALLOW;
	}
	
	private static class PlaceHolder extends Restlet {
		
		private final int accessMode;
		
		public PlaceHolder(int accessMode) {
			this.accessMode = accessMode;
		}
		
		public int getAccessMode() {
			return accessMode;
		}
		
	}

}
