/*******************************************************************************
 * Copyright (C) 2007-2009 Solertium Corporation
 * 
 * This file is part of the open source GoGoEgo project.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 *  
 * 1) The Eclipse Public License, v.1.0
 *     http://www.eclipse.org/legal/epl-v10.html
 * 
 *  2) The GNU General Public License, version 2 or later
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package org.gogoego.api.authentication;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public abstract class CookieAuthenticatorActivator implements BundleActivator {
	
	public abstract CookieAuthenticatorFactory getService();
	
	public void start(BundleContext context) throws Exception {
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Constants.SERVICE_PID, getClass().getName());
		
		final CookieAuthenticatorFactory service = getService();
		context.registerService(CookieAuthenticatorFactory.class.getName(), service, props);
	}
	
	public void stop(BundleContext context) throws Exception {
		
	}

}
