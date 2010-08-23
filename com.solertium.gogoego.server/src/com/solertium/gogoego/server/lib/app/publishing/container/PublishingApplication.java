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
package com.solertium.gogoego.server.lib.app.publishing.container;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.GoGoEgoApplicationMetaData;
import org.gogoego.api.applications.HasSettingsUI;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.solertium.gogoego.server.lib.app.publishing.resources.CollectionPublishingExportResource;
import com.solertium.gogoego.server.lib.app.publishing.resources.CollectionPublishingImportResource;
import com.solertium.gogoego.server.lib.app.publishing.resources.CollectionPublishingNotificationResource;
import com.solertium.gogoego.server.lib.app.publishing.resources.CollectionPublishingSettingsResource;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class PublishingApplication extends GoGoEgoApplication implements
		HasSettingsUI, GoGoEgoApplicationMetaData, GoGoEgoApplicationManagement {
	
	public static final String REGISTRATION = 
		PublishingApplication.class.getName();
	
	private CollectionPublishingSettings settings;
	private final AtomicBoolean isPublishing = new AtomicBoolean(false);

	public Restlet getPrivateRouter() {
		final Router root = new Router(app.getContext());
		root.attach("/export", CollectionPublishingExportResource.class);
		root.attach("/settings", CollectionPublishingSettingsResource.class);
		return root;
	}
	
	public Restlet getPublicRouter() {
		final Router root = new Router(app.getContext());
		root.attach("/import", CollectionPublishingImportResource.class);
		root.attach("/notify", CollectionPublishingNotificationResource.class);
		return root;
	}
	
	public boolean isInstalled() {
		settings = new CollectionPublishingSettings(app.getVFS());
		try {
			settings.init();
		} catch (GoGoEgoApplicationException e) {
			return false;
		}
		return true;
	}

	public String getSettingsURL() {
		return "local";
	}
	
	public String getDescription() {
		return "Targeted Collection Publishing from one GoGoEgo Site to another.  Simply install " +
				"this application on both sites, configure one to support importing, and you're all " +
				"set up.";
	}
	
	public String getName() {
		return "Collection Publishing";
	}
	
	public void install(VFS vfs) throws GoGoEgoApplicationException {
		try {
			vfs.makeCollections(new VFSPath("/(SYSTEM)/publishing"));
		} catch (IOException e) {
			throw new GoGoEgoApplicationException(e);
		}
	}
	
	public void uninstall(VFS vfs) throws GoGoEgoApplicationException {
		try {
			vfs.delete(new VFSPath("/(SYSTEM)/publishing"));
		} catch (IOException ignored) {
			TrivialExceptionHandler.ignore(this, ignored);
		}
	}
	
	/**
	 * FIXME: remove this when the app is done.
	 */
	public boolean isSiteAllowed(Context context, String siteID) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public CollectionPublishingSettings getSettings() {
		return settings;
	}
	
	public AtomicBoolean getPublishingMarker() {
		return isPublishing;
	}

}
