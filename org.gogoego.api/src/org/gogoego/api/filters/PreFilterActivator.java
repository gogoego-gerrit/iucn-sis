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
package org.gogoego.api.filters;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * PreFilterActivator.java
 * 
 * A simple activator that can be subclassed to guarantee that your 
 * prefilter is instantiated correctly within GoGoEgo.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class PreFilterActivator implements BundleActivator {
	
	/**
	 * Retrieves the PreFilterFactory for use.
	 * @return
	 */
	public abstract PreFilterFactory getService();
	
	public void start(BundleContext context) throws Exception {
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Constants.SERVICE_PID, getClass().getName());
		
		final PreFilterFactory service = getService();
		context.registerService(PreFilterFactory.class.getName(), service,
				props);
	}

	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}
	
}
