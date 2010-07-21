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
package com.solertium.gogoego.server.lib.services;

import java.util.HashMap;

import org.gogoego.api.applications.ApplicationEventsAPI;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Route;
import org.restlet.routing.Router;


/**
 * ApplicationEvents.java
 * 
 * Hook listeners to events that happen at a given URI 
 * for a particular request method.
 * 
 * @author carl.scott
 * 
 */
public class ApplicationEvents implements ApplicationEventsAPI {

	private final HashMap<Method, Router> map;
	private final Context context;

	public ApplicationEvents(Context context) {
		map = new HashMap<Method, Router>();
		this.context = context;
	}

	public void register(Method method, String uriPattern, EventHandler handler) {
		Router router = map.get(method);
		if (router == null)
			router = new Router(context);

		router.attach(uriPattern, handler);

		map.put(method, router);
	}

	public void fireEvent(Context context, Request request, Response response) {
		final Response fauxResponse = new Response(request);
		Router router = map.get(request.getMethod());
		if (router != null) {
			Route next = (Route) router.getNext(request, fauxResponse);
			if (next != null) {
				try {
					((EventHandler) next.getNext()).handle(context, request, response);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
