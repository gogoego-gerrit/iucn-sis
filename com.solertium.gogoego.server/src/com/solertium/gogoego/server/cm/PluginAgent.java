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
/* This file in its present form originates with Apache Felix,
 * which in turn descends from an original work by Peter
 * Kriens.  The license for this work follows; this agent does
 * not have the same restrictions as other GoGoEgo code.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.solertium.gogoego.server.cm;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.gogoego.api.filters.FileWritingFilterFactory;
import org.gogoego.api.filters.PreFilterFactory;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

import com.solertium.gogoego.server.GoGoDebug;

/**
 * PluginAgent.java
 * 
 * Initialize brokers and service trackers.
 * 
 * @author rob.heittman
 *
 */
public class PluginAgent implements BundleActivator, ManagedServiceFactory {
	static ServiceTracker padmin;
	static ServiceTracker cmTracker;
	
	static ScriptableObjectBroker broker;
	static GoGoEgoApplicationBroker appBroker;
	static ClassLoaderBroker clBroker;
	static ImageManipulatorBroker imBroker;
	static ScriptEngineBroker scriptingBroker;
	static AuthenticatorBroker authBroker;
	static CookieAuthenticatorBroker cookieAuthBroker;	
	static ErrorHandlerBroker errorHandlerBroker;
	static PluginBroker<FileWritingFilterFactory> fileWritingFilterBroker;
	static PluginBroker<PreFilterFactory> preFilterBroker;
	
	static BundleManagementBroker bmBroker;
	
	static LightDirectoryWatcher watcher;

	BundleContext context;
	
	private static PluginAgent impl;

	public void deleted(final String pid) {
		if(watcher!=null) watcher.stop();
	}

	public String getName() {
		return getClass().getName();
	}

	public static ScriptableObjectBroker getScriptableObjectBroker() {
		return broker;
	}

	public static GoGoEgoApplicationBroker getGoGoEgoApplicationBroker() {
		return appBroker;
	}
	
	public static ClassLoaderBroker getClassLoaderBroker() {
		return clBroker;
	}
	
	public static ImageManipulatorBroker getImageManipulatorBroker() {
		return imBroker;
	}
	
	public static BundleManagementBroker getBundleManagementBroker() {
		return bmBroker;
	}
	
	public static AuthenticatorBroker getAuthenticatorBroker() {
		return authBroker;
	}
	
	public static CookieAuthenticatorBroker getCookieAuthenticatorBroker() {
		return cookieAuthBroker;
	}
	
	public static ScriptEngineBroker getScriptEngineBroker() {
		return scriptingBroker;
	}
	
	public static ErrorHandlerBroker getErrorHandlerBroker() {
		return errorHandlerBroker;
	}
	
	public static PluginBroker<FileWritingFilterFactory> getFileWritingFilterBroker() {
		return fileWritingFilterBroker;
	}
	
	public static PluginBroker<PreFilterFactory> getPreFilterBroker() {
		return preFilterBroker;
	}

	private void set(final Hashtable<String, Object> ht, final String key) {
		Object o = context.getProperty(key);
		if (o == null) {
			return;
		}
		ht.put(key, o);
	}

	public void start(final BundleContext iContext) throws Exception {
		impl = this;
		impl.context = iContext;
	}
	
	public static void init() throws Exception {
		impl.doInit();
	}
	
	private void doInit() throws Exception {
		GoGoDebug.system().println("GoGoEgo OSGi plugin agent starting");
		
		appBroker = new GoGoEgoApplicationBroker(context);
		authBroker = new AuthenticatorBroker(context);
		clBroker = new ClassLoaderBroker(context);
		cookieAuthBroker = new CookieAuthenticatorBroker(context);
		broker = new ScriptableObjectBroker(context);
		imBroker = new ImageManipulatorBroker(context);
		scriptingBroker = new ScriptEngineBroker(context);
		errorHandlerBroker = new ErrorHandlerBroker(context);
		fileWritingFilterBroker = new PluginBroker<FileWritingFilterFactory>(context, FileWritingFilterFactory.class.getName());
		preFilterBroker = new PluginBroker<PreFilterFactory>(context, PreFilterFactory.class.getName());

		bmBroker = new BundleManagementBroker(context);
		
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Constants.SERVICE_PID, getName());
		if (context != null)
		context.registerService(ManagedServiceFactory.class.getName(), this, props);

		if (context != null) {
		padmin = new ServiceTracker(context, PackageAdmin.class.getName(), null);
		padmin.open();
		cmTracker = new ServiceTracker(context, ConfigurationAdmin.class.getName(), null);
		cmTracker.open();
		
		}
		// Created the initial configuration
		final Hashtable<String, Object> ht = new Hashtable<String, Object>();
		final Properties init = GoGoEgo.getInitProperties();
		if (init != null) 
			for (Map.Entry<Object, Object> entry : init.entrySet())
				ht.put(entry.getKey().toString(), entry.getValue());

		if (context != null) {
			set(ht, LightDirectoryWatcher.DIR);
			updated("initial", ht);
		}
	}

	public void stop(final BundleContext context) throws Exception {
		if(watcher!=null) watcher.stop();
		cmTracker.close();
		padmin.close();
	}

	@SuppressWarnings("unchecked")
	public void updated(final String pid, final Dictionary properties) throws ConfigurationException {
		deleted(pid);
		watcher = new LightDirectoryWatcher(properties,context);
		new Thread(watcher).start();
	}
}
