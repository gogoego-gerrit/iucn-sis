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

import org.restlet.Restlet;

/**
 * Interface for creating a GoGoEgo Application
 * 
 * @author carl.scott
 * 
 */
public abstract class GoGoEgoApplication {

	protected ServerApplicationAPI app;
	private String registrationKey;

	/**
	 * Initialize the application with information about the server and 
	 * the registration key used to locate and mount this application
	 * 
	 * @param application
	 * @param registrationKey
	 */
	public final void init(final ServerApplicationAPI application, String registrationKey) {
		this.app = application;
		this.registrationKey = registrationKey;
	}

	/**
	 * Determines whether or not this application is installed for this
	 * particular site.
	 * 
	 * @return true if application is installed and can be added to the site,
	 *         false otherwise
	 */
	public boolean isInstalled() {
		return true;
	}

	/**
	 * The path that the application will attach to. Applications should attach
	 * to /apps/{appID}. Future restrictions may be placed on this functionality
	 * 
	 * @return the path
	 */
	public final String getPath() {
		return "/apps/" + registrationKey;
	}

	/**
	 * Return the registration key (typically the bundle's symbolic name).
	 * @return the key
	 */
	public final String getRegistrationKey() {
		return registrationKey;
	}

	/**
	 * Build the publicly-accessible router. The router returned will be
	 * attached to your site at the root specified by getPath()
	 * @return the restlet, or null if not applicable
	 */
	public abstract Restlet getPublicRouter();

	/**
	 * Build the privately-accessible router. The router returned will be
	 * attached to your site behind /admin at the root specified by getPath().
	 * This means that users will need to login with a user name and password
	 * to access these resources. Additionally, this Restlet will be placed
	 * behind the MagicDisablingFilter, so any resources will not run to
	 * GoGoMagicFilter's parsing.
	 * @return the restlet, or null if not applicable
	 */
	public abstract Restlet getPrivateRouter();

}
