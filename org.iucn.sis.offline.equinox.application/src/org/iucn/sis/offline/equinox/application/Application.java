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
 *    http://www.eclipse.org/legal/epl-v10.html
 * 
 * 2) The GNU General Public License, version 2 or later
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package org.iucn.sis.offline.equinox.application;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.ProductProperties;
import org.restlet.engine.Engine;

import com.solertium.db.DBException;
import com.solertium.gogoego.server.Bootstrap;
import com.solertium.gogoego.server.GoGoBackboneImpl;
import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.Restarter;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.CurrentBinary;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.DesktopIntegration;

public class Application implements IApplication, Restarter {

	Bootstrap component;
	
	int stopRequested = 0;
	Thread idlingThread;
	
	public Object start(IApplicationContext context) throws Exception {
		/*
		 * Hack? We must first set the current working directory 
		 * in order to later load StandardServerComponent properties 
		 * from the correct directory, should the use have specified 
		 * a relative path.  If an absolute path is specified, this 
		 * is not necessary.
		 */
		ProductProperties.impl.setWorkingDirectory(CurrentBinary.getDirectory(this));
		
		/*
		 * Set up GoGoEgo object and debugging.
		 */
		GoGoDebug.init();
		GoGoEgo.build(new GoGoBackboneImpl());
		
		/*
		 * Now that the working directory is known, we can *actually* 
		 * start the PluginAgent.  Note that OSGi has already started 
		 * this bundle (read: called the BundleActivator.start() method), 
		 * but no work has been performed.
		 */
		PluginAgent.init();
		
		//Two hacks down!
		/*System.out.println("Initializing Jython");
		Properties props = new Properties();
		props.setProperty("python.path", "/var/lib/jython");
		PythonInterpreter.initialize(System.getProperties(), props, new String[] {""});
		System.out.println("Done initializing Jython");*/
		Engine.setUserClassLoader(this.getClass().getClassLoader());
		//One hack down! 
		//new Clapper().initClap();
		try {
			component = new Bootstrap();
			component.setRestarter(this);
		} catch (DBException e) {
			System.err.println("Could not start DBSession: " + e.getMessage());
		}

		if ("true".equals(GoGoEgo.getInitProperties().getProperty("GOGOEGO_DESKTOP"))) {
			String appName = GoGoEgo.getInitProperties().getProperty("GOGOEGO_DESKTOP_NAME", "GoGoEgo");
			String startUrl = GoGoEgo.getInitProperties().getProperty("GOGOEGO_DESKTOP_STARTURL", "/admin/index.html");
			
			try {
				DesktopIntegration.launch(appName, startUrl, component.getIconProvider(), component);
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		else {
			try {
				component.start();
			} catch (final Exception startupException) {
				startupException.printStackTrace();
			}
		}
		
		// idle needed
		idlingThread = Thread.currentThread();
		while(stopRequested==0){
			try {
				Thread.sleep(100000);
			} catch (InterruptedException interrupted) {
				// that's fine
			}
		}
		if(stopRequested == 1){
			return EXIT_OK;
		} else {
			return EXIT_RESTART;
		}
	}

	public void stop() {
		try{
			component.stop();
			stopRequested = 1;
			if(idlingThread!=null) idlingThread.interrupt();
		} catch (final Exception stopException) {
			stopException.printStackTrace();
		}
	}

	public void restart() {
		try{
			component.stop();
			stopRequested = -1;
			if(idlingThread!=null) idlingThread.interrupt();
		} catch (final Exception stopException) {
			stopException.printStackTrace();
		}
	}

}
