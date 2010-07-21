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
package org.gogoego.api.scripting;

import java.util.Hashtable;

import org.gogoego.api.plugins.GoGoEgo;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import com.solertium.util.SysDebugger;

/**
 * ScriptableObjectActivator.java
 * 
 * A simple activator that can be subclassed to guarantee that your 
 * scriptable object is instantiated correctly within GoGoEgo.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class ScriptableObjectActivator implements BundleActivator {
	
	/**
	 * Retrieves the ScriptableObject for use.
	 * @return
	 */
	public abstract ScriptableObjectFactory getService();

	public final void start(BundleContext context) throws Exception {
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Constants.SERVICE_PID, getClass().getName());
		
		final ScriptableObjectFactory service = getService();
		context.registerService(ScriptableObjectFactory.class.getName(), service,
				props);
		SysDebugger.getInstance().println("Successfully registered " +service.getClass().getName());
		
		GoGoEgo.getCacheHandler().clearCache();
	}

	public final void stop(BundleContext context) throws Exception {
	}

}
