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
package com.solertium.gogoego.server.lib.app.writefilter.container;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationMetaData;
import org.gogoego.api.applications.HasSettingsUI;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.solertium.gogoego.server.lib.app.writefilter.resources.FileWritingFilterListingResource;
import com.solertium.gogoego.server.lib.app.writefilter.resources.FileWritingFilterManagementResource;
import com.solertium.gogoego.server.lib.app.writefilter.resources.FileWritingFilterSettingsResource;

public class FileWritingFilterApplication extends GoGoEgoApplication implements GoGoEgoApplicationMetaData, HasSettingsUI {
	
	public static final String REGISTRATION = "com.solertium.gogoego.server.lib.app.writefilter.container.FileWritingFilterApplication";
	
	public static FileWritingFilterApplication getInstance(Context context) {
		return (FileWritingFilterApplication)GoGoEgo.get().getApplication(context, REGISTRATION);
	}

	public Restlet getPrivateRouter() {
		final Router router = new Router(app.getContext());
		router.attach("/settings", FileWritingFilterSettingsResource.class);
		router.attach("/list", FileWritingFilterListingResource.class);
		router.attach("/manage/{filterID}", FileWritingFilterManagementResource.class);
		
		return router;
	}

	public Restlet getPublicRouter() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getSettingsURL() {
		return "local";
	}
	
	public FileWritingFilterSettings getSettings() {
		return new FileWritingFilterSettings(app.getVFS());
	}
	
	public String getDescription() {
		return "Allows your files to be run through filters before saving.";
	}
	
	public String getName() {
		return "File Writing Filter";
	}

}
