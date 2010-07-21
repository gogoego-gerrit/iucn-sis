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
package com.solertium.util.restlet.authorization.base;

import java.util.Map;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

/**
 * Authorizer.java
 * 
 * Checks a structure document for information regarding 
 * accessibility of a given method for a given uri.
 *
 * @author carl.scott <carl.scott@solertium.com>
 *
 */
public final class BaseAuthorizer implements Authorizer {
	
	private final Router router;
	private Boolean defaultAccessBehavior;
	
	public BaseAuthorizer(final Structure struct) {
		router = new Router();
		
		final Map<String, AuthorizableObject> map = struct.getMapping();
		for (Map.Entry<String, AuthorizableObject> entry : map.entrySet()) {
			final AuthorizableObjectWrapper wrapper = 
				new AuthorizableObjectWrapper(entry.getValue());
			for (String uri : entry.getValue().getUris())
				router.attach(uri, wrapper);
		}
		
		setDefaultAccessBehavior(true);
	}
	
	public boolean isAuthorized(final String uri, final String actor, final String action) {
		final Request fauxRequest = new Request(Method.GET, new Reference(uri));
		final Route wrapper = 
			(Route)router.getNext(fauxRequest, new Response(fauxRequest));
		
		if (wrapper == null) {
			System.out.println("No authz data found for " + fauxRequest.getResourceRef() + ", do default behavior.");
			return defaultAccessBehavior.booleanValue();
		}
		
		final AuthorizableObject obj = ((AuthorizableObjectWrapper)wrapper.getNext()).obj;
		
		return obj.getAllowedActions().contains(action);
	}
	
	/**
	 * @param defaultAccessBehavior the defaultAccessBehavior to set
	 */
	public void setDefaultAccessBehavior(boolean defaultAccessBehavior) {
		this.defaultAccessBehavior = Boolean.valueOf(defaultAccessBehavior);
	}
	
	protected static class AuthorizableObjectWrapper extends Restlet {
		
		private final AuthorizableObject obj;
		
		public AuthorizableObjectWrapper(final AuthorizableObject obj) {
			this.obj = obj;
		}
		
		public AuthorizableObject getObject() {
			return obj;
		}
		
	}

}
