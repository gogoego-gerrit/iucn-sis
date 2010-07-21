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
package org.gogoego.api.applications;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * ApplicationEventsAPI.java
 * 
 * An API for registering listeners to GoGoEgo.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface ApplicationEventsAPI {
	
	/**
	 * Registers a listen.  Give an HTTP method to listen for, the  
	 * pattern of the URI as used in org.restlet.Router, and your 
	 * event handler.
	 * @param method
	 * @param uriPattern
	 * @param handler
	 */
	public void register(Method method, String uriPattern, EventHandler handler);
	
	/**
	 * EventHandler
	 * 
	 * Interface to handle events as they occur.
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
	 *         href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	public static abstract class EventHandler extends Restlet {
		public abstract void handle(Context context, Request request, Response response);
	}

}
