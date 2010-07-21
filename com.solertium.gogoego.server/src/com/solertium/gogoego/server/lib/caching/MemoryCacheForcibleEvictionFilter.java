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

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * MemoryCacheForcibleEvictionFilter.java
 * 
 * Will clear the entire cache upon any write operation. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class MemoryCacheForcibleEvictionFilter extends MemoryCacheEvictionFilter {

	MemoryCacheForcibleEvictionFilter(Context context) {
		super(context);
	}
	
	MemoryCacheForcibleEvictionFilter(Context context, Restlet next) {
		super(context, next);
	}

	protected void afterHandle(Request request, Response response) {
		if (writeMethods.contains(request.getMethod()) && response.getStatus().isSuccess()) {
			MemoryCache.getInstance().clear(getContext());
		}
	}
}
