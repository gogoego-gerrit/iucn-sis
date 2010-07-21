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
package com.solertium.gogoego.server.applications;

import org.gogoego.api.applications.ServerApplicationAPI;
import org.restlet.Context;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.services.ApplicationEvents;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;
import com.solertium.util.restlet.ScratchResourceBin;
import com.solertium.vfs.VFS;

/**
 * ServerApplicationAPI.java
 * 
 * Provides a short API to the ServerApplication for classes that extend
 * GoGoEgoApplication. All the popular and necessary getters are accessible via
 * this class.
 * 
 * @author carl.scott
 * 
 */
public class ServerApplicationImpl implements ServerApplicationAPI {

	private final ServerApplication app;

	public ServerApplicationImpl(final ServerApplication app) {
		this.app = app;
	}

	/**
	 * Use this to create restlets and resources in the appropriate application
	 * context
	 * 
	 * @return
	 */
	public Context getContext() {
		return app.getContext();
	}

	/**
	 * Provides access to the template registry, so you can append or inquire
	 * template
	 * 
	 * @return
	 */
	public TemplateRegistry getTemplateRegistry() {
		return app.getTemplateRegistry();
	}

	/**
	 * A public scratch resource bin which provides transient data storage that
	 * can be backed up and have automated cleanup based on a provided
	 * expiration date.
	 * 
	 * @return
	 */
	public ScratchResourceBin getScratchResourceBin() {
		return app.getScratchResourceBin();
	}

	/**
	 * Register and fire application events, to allow things to happen when
	 * actions take place at given uri-spaces.
	 * 
	 * @return
	 */
	public ApplicationEvents getApplicationEvents() {
		return app.getApplicationEvents();
	}

	/**
	 * Access the site's VFS
	 * 
	 * @return
	 */
	public VFS getVFS() {
		return app.getVFS();
	}

	/**
	 * Get the ID of this site.
	 * 
	 * @return
	 */
	public String getSiteID() {
		return app.getInstanceId();
	}

	/**
	 * Get the HTTPS host of this site
	 * 
	 * @return
	 */
	public String getHttpsHost() {
		return app.getHttpsHost();
	}

	/**
	 * Determine if GoGoEgo is running in hosted mode, useful when developing
	 * when you want to bypass guards and authentication steps.
	 * 
	 * @return
	 */
	public boolean isHostedMode() {
		return app.isHostedMode();
	}

}
