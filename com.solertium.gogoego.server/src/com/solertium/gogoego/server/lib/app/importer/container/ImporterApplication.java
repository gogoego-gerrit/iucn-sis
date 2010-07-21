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
package com.solertium.gogoego.server.lib.app.importer.container;

import java.io.IOException;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.HasSettingsUI;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.solertium.gogoego.server.lib.app.importer.resources.ImportAppResource;
import com.solertium.gogoego.server.lib.app.importer.resources.ImportSettingsResource;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ImporterApplication.java
 * 
 * @author carl.scott
 * 
 */
public class ImporterApplication extends GoGoEgoApplication implements GoGoEgoApplicationManagement, HasSettingsUI {

	public static final String REGISTRATION = "com.solertium.gogoego.server.lib.app.importer.container.ImporterApplication";
	
	private ImporterSettings settings;

	public Restlet getPublicRouter() {
		Router root = new Router(app.getContext());
		
		root.attach("/import", ImportAppResource.class);

		return new MagicDisablingFilter(app.getContext(), root);
	}

	public Restlet getPrivateRouter() {
		Router root = new Router(app.getContext());
		
		root.attach("/direct", ImportAppResource.class);
		root.attach("/settings", ImportSettingsResource.class);

		return new MagicDisablingFilter(app.getContext(), root);
	}

	public VFS getVFS() {
		return app.getVFS();
	}

	public void install(VFS vfs) throws GoGoEgoApplicationException {
		try {
			vfs.makeCollections(new VFSPath("/(SYSTEM)/importer"));
		} catch (IOException e) {
			throw new GoGoEgoApplicationException(e);
		}
	}

	public boolean isInstalled() {
		settings = new ImporterSettings(app.getVFS());
		try {
			settings.init();
		} catch (GoGoEgoApplicationException e) {
			return false;
		}		
		return true;
	}

	public void uninstall(VFS vfs) throws GoGoEgoApplicationException {
		try {
			vfs.delete(new VFSPath("/(SYSTEM)/importer"));
		} catch (IOException ignored) {
			TrivialExceptionHandler.ignore(this, ignored);
		}
	}
	
	public ImporterSettings getSettings() {
		return settings;
	}
	
	public String getSettingsURL() {
		return "local";
	}

}
