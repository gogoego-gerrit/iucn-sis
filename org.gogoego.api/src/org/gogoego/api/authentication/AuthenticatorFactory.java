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
package org.gogoego.api.authentication;

import org.gogoego.api.applications.ServerApplicationAPI;

import com.solertium.util.restlet.authentication.Authenticator;

/**
 * AuthenticatorFactory.java
 * 
 * Creates a new instance of an Authenticator, to be used in the 
 * context of an OSGi plugin to allow users to authenticate 
 * against the given authenticator to log into GoGoEgo. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface AuthenticatorFactory {

	/**
	 * Creates a new instance of an Authenticator given the 
	 * context and the site ID.  Given the context, you should 
	 * be able to access GoGoEgo's ServerApplicationAPI if there 
	 * is server-specific information you need to create your 
	 * Authenticator.
	 * 
	 * @param context the context
	 * @param siteID the site ID
	 * @return the new authenticator
	 */
	public Authenticator newInstance(ServerApplicationAPI api);
	
}
