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
package com.solertium.gogoego.server.filters;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

import com.solertium.util.restlet.RestletUtils;

public class CacheControlFilter extends Filter {
	
	public CacheControlFilter(Context context) {
		super(context);
	}
	
	protected void afterHandle(Request request, Response response) {
		if (!response.isEntityAvailable())
			return;
		
		final Representation entity = response.getEntity();
		
		GoGoEgo.debug().println("-------------");
		GoGoEgo.debug().println("Cache Information for {0}", request.getResourceRef());
		GoGoEgo.debug().println("Last Modified: {0}", entity.getExpirationDate());
		GoGoEgo.debug().println("Cache Control: {0}", RestletUtils.getHeader(response, "Cache-control"));
		GoGoEgo.debug().println("Response headers: {0}", RestletUtils.getHeaders(response));
		GoGoEgo.debug().println("-------------");
		
		response.setEntity(entity);
	}

}
