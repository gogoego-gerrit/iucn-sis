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

import org.gogoego.api.utils.LastModifiedDisablingFilter;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;


/**
 * BasicGoGoEgoApplication.java
 * 
 * Quickly create a GoGoEgoApplication from a Restlet Application
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class BasicGoGoEgoApplication extends GoGoEgoApplication {
		
	public abstract Application getApplication(Context context);

	public Restlet getPrivateRouter() {
		return null;
	}

	public Restlet getPublicRouter() {
		try {
			final Context context = app.getContext().createChildContext();
			final Restlet next = new LastModifiedDisablingFilter(context, new MagicDisablingFilter(
				context, getApplication(context)
			));
			return new ApplicationFilter(context, next);
			
		} catch (Exception e) {
			return new Restlet(app.getContext()) {
				public void handle(Request request, Response response) {
					response.setEntity(new StringRepresentation(
						"Application could not be installed due to initialization error."
					));
				}
			};
		}
	}

	public boolean isInstalled() {
		return true;
	}
	
	public static class ApplicationFilter extends Filter {
		
		private Application gogoego;
		
		public ApplicationFilter(Context context, Restlet next) {
			super(context, next);
		}
		
		protected int beforeHandle(Request request, Response response) {
			gogoego = Application.getCurrent();
			return Filter.CONTINUE;
		}
		
		protected void afterHandle(Request request, Response response) {
			Application.setCurrent(gogoego);
		}
		
	}

}
