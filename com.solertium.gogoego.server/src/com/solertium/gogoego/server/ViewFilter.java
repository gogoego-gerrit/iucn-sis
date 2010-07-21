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

package com.solertium.gogoego.server;

import org.gogoego.api.collections.Constants;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

/**
 * ViewFilter.java
 * 
 * Provides filters for GoGoEgo to listen for when the client makes a 
 * special request, such as viewing a page in edit mode, surfing mode, 
 * or to view the page tree. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ViewFilter extends Filter {

	public static final String PAGE_TREE = "com.solertium.gogoego.server.pageTree";
	public static final String SHOW_TREE = "com.solertium.gogoego.server.displayPageTree";
	public static final String SURFMODE = "surf";

	public ViewFilter(final Context context) {
		super(context);
	}

	@Override
	public int beforeHandle(final Request request, final Response response) {
		allowGZIP(request);
		
		final String mode = request.getResourceRef().getQueryAsForm().getFirstValue("ggeviewmode");
		if ((mode != null) && !mode.equals("")) {
			if (mode.equalsIgnoreCase("edit"))
				request.getAttributes().put(Constants.EDITMODE, true);
			else if (mode.equals(SURFMODE))
				request.getAttributes().put(ViewFilter.SURFMODE, true);
			else if (mode.equals("pagetree"))
				request.getAttributes().put(SHOW_TREE, Boolean.TRUE);
			else
				return Filter.CONTINUE;

			final String query = request.getResourceRef().getQuery();

			if (query.equalsIgnoreCase("ggeviewmode=" + mode))
				request.getResourceRef().setQuery(null);
			else if (query.startsWith("ggeviewmode=" + mode))
				request.getResourceRef().setQuery(query.substring(("?ggeviewmode=" + mode).length()));
			else
				request.getResourceRef().setQuery(query.replaceFirst("ggeviewmode=" + mode, ""));
		}

		return Filter.CONTINUE;
	}
	
	/**
	 * Only added this here to avoid having to make yet another 
	 * filter object.  Adding a flag to allow for public resources 
	 * to use GZIP.  I don't want resources served from the private 
	 * router to use GZIP unless explicitly requested.
	 * @param request
	 */
	private void allowGZIP(Request request) {
		request.getAttributes().put(Constants.ALLOW_GZIP, Boolean.TRUE);
	}
}
