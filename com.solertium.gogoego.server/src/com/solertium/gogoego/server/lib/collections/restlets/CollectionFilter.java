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

package com.solertium.gogoego.server.lib.collections.restlets;

import java.util.ArrayList;

import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

import com.solertium.util.restlet.RestletUtils;

/**
 * CollectionFilter.java
 * 
 * Filters operations on collections.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionFilter extends Filter {

	private String applicationKey;
	private ArrayList<Method> notMagical;

	public CollectionFilter(final Context context, final String applicationKey) {
		super(context);
		setNext(CollectionResource.class);

		this.applicationKey = applicationKey;

		notMagical = new ArrayList<Method>();
		notMagical.add(Method.PROPFIND);
		notMagical.add(Method.COPY);
	}

	@Override
	public int beforeHandle(final Request request, final Response response) {
		if (isNotMagical(request.getMethod())) {
			request.getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
		}

		return isAuthorized(request) ? Filter.CONTINUE : Filter.STOP;
	}

	private boolean isNotMagical(Method method) {
		return notMagical.contains(method);
	}

	private boolean isAuthorized(final Request request) {
		Method m = request.getMethod();
		if (m.equals(Method.GET) || m.equals(Method.PROPFIND))
			return true;

		/*
		 * This portion should no longer be needed.
		 */
		String header = "applicationKey";
		String ret = RestletUtils.getHeader(request, header);

		return ret != null && ret.equals(applicationKey);
	}

}
