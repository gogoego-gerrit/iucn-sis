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
package com.solertium.gogoego.server.cm;

import org.gogoego.api.authentication.AuthenticatorFactory;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleContext;

/**
 * AuthenticatorBroker.java
 * 
 * Broker authenticators.  Each authenticator will be given the realm of 
 * its bundle's symbolic name.  
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class AuthenticatorBroker extends PluginBroker<AuthenticatorFactory> {
	
	public AuthenticatorBroker(final BundleContext bundleContext) {
		super(bundleContext, AuthenticatorFactory.class.getName());
	}

}
