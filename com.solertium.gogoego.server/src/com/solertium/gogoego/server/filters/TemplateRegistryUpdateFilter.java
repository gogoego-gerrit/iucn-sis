/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.filters;

import java.util.Collection;
import java.util.HashSet;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;

public class TemplateRegistryUpdateFilter extends Filter {
	
	private static final String FILTER_DIR = "/admin/files/templates";
	
	private final Collection<Method> allowed;
	
	public TemplateRegistryUpdateFilter(Context context) {
		super(context);
		allowed = new HashSet<Method>();
		allowed.add(Method.POST);
		allowed.add(Method.PUT);
		allowed.add(Method.DELETE);
	}

	protected void afterHandle(Request request, Response response) {
		if (allowed.contains(request.getMethod()) && response.getStatus().isSuccess() && 
				request.getResourceRef().getPath().startsWith(FILTER_DIR)) {
			GoGoEgo.getCacheHandler().removeFromCache(getContext(), request.getResourceRef().getRemainingPart());
			//If above fails try this:
			//GoGoEgo.getCacheHandler().clearCache();
			
			TemplateRegistry registry = 
				ServerApplication.getFromContext(getContext()).getTemplateRegistry();
			registry.refresh();
		}
	}
	
}
