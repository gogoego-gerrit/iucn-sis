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
package org.gogoego.api.filters;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * PreFilter.java
 * 
 * Used to interrogate and perform actions upon requests before 
 * they are handled by GoGoEgo processing.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface PreFilter {
	
	public static enum FilterProcessing {
		CONTINUE, STOP
	};
	
	/**
	 * Handle an incoming request.  Then, return whether or not to continue 
	 * this request or stop and return to the client.
	 * @param context
	 * @param request
	 * @param response
	 * @return true to continue processing, false otherwise
	 */
	public FilterProcessing handle(Context context, Request request, Response response);

}
