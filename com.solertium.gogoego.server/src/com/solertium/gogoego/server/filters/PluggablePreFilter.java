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
package com.solertium.gogoego.server.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.gogoego.api.collections.GenericCollection;
import org.gogoego.api.collections.GoGoEgoItem;
import org.gogoego.api.filters.PreFilter;
import org.gogoego.api.filters.PreFilterFactory;
import org.gogoego.api.filters.PreFilter.FilterProcessing;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * PluggablePreFilter.java
 * 
 * This filter can be used with OSGi plugins to perform operations 
 * at the very topmost level for any given request.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class PluggablePreFilter extends Filter {
	
	public PluggablePreFilter(Context context, Restlet next) {
		super(context, next);
	}
	
	protected int beforeHandle(Request request, Response response) {
		if (!(Protocol.HTTP.equals(request.getProtocol()) || Protocol.HTTPS.equals(request.getProtocol())))
			return Filter.CONTINUE;
				
		final VFS vfs = GoGoEgo.get().getFromContext(getContext()).getVFS();
		final VFSPath path = new VFSPath("/(SYSTEM)/prefilter/sort.xml");
		
		Document document = null;
		if (vfs.exists(path)) {
			try {
				document = vfs.getDocument(path);
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		
		final Map<String, PreFilterFactory> map = 
			PluginAgent.getPreFilterBroker().getPlugins();
		
		final Collection<PreFilterFactory> plugins;
		if (document != null) {
			plugins = new ArrayList<PreFilterFactory>();
			final GenericCollection collection = new GenericCollection(document);
			for (GoGoEgoItem item : collection.getItems().values()) {
				PreFilterFactory plugin = map.remove(item.getItemName());
				if (plugin != null)
					plugins.add(plugin);
			}
			plugins.addAll(map.values());
		}
		else {
			plugins = new ArrayList<PreFilterFactory>();
			plugins.addAll(map.values());
		}
		
		FilterProcessing doContinue = FilterProcessing.CONTINUE;
		for (PreFilterFactory factory : plugins) {
			final PreFilter filter = factory.newInstance();
			
			try {
				doContinue = filter.handle(getContext(), request, response);
			} catch (Throwable e) {
				continue;
			}

			if (FilterProcessing.STOP.equals(doContinue))
				break;
		}
		
		return FilterProcessing.STOP.equals(doContinue) ? Filter.STOP : Filter.CONTINUE;
	}

}
