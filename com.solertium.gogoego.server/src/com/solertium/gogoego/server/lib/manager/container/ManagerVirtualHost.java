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
package com.solertium.gogoego.server.lib.manager.container;

import java.util.ArrayList;

import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;
import org.restlet.routing.VirtualHost;

import com.solertium.gogoego.server.Bootstrap;

/**
 * ManagerVirtualHost.java
 * 
 * The manager application has a user-defined resource space, which is defined
 * in the component-config. From there, the ManagerApplication is attach to that
 * resource space at the component level -- essentially, it is its own website.
 * 
 * @author carl.scott
 * 
 */
public class ManagerVirtualHost extends VirtualHost {

	public ManagerVirtualHost(final Context context, final String resourceDomain, final String siteID,
			final String hostRoot, final ArrayList<String> sitesOnServer, final Bootstrap bootstrap,
			final boolean isInternal) {
		super(context);
		setResourceDomain(resourceDomain);

		final ManagerApplication application = new ManagerApplication(context, siteID, hostRoot, sitesOnServer,
				bootstrap);

		if (isInternal) {
			final ManagerInternalFilter filter = new ManagerInternalFilter(context);
			filter.setNext(application);

			attach(filter);
		} else
			attach(application);
	}

	public static class ManagerInternalFilter extends Filter {

		public ManagerInternalFilter(Context context) {
			super(context);
		}

		protected int beforeHandle(Request request, Response response) {
			return request.getProtocol().equals(Protocol.RIAP) ? Filter.CONTINUE : Filter.STOP;
		}
	}
}
