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
package com.solertium.gogoego.equinox.agent;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This clever little bundle watches a directory and will install any jar file
 * if finds in that directory (as long as it is a valid bundle and not a
 * fragment).
 * 
 */
public class DirectoryAgent implements BundleActivator, ManagedServiceFactory {
	static ServiceTracker padmin;
	static ServiceTracker cmTracker;
	BundleContext context;
	Map<String, DirectoryWatcher> watchers = new HashMap<String, DirectoryWatcher>();

	public void deleted(final String pid) {
		final DirectoryWatcher watcher = watchers.remove(pid);
		if (watcher != null)
			watcher.close();
	}

	public String getName() {
		return "com.solertium.gogoego.equinox.agent.directoryagent";
	}

	private void set(final Hashtable<String, Object> ht, final String key) {
		Object o = context.getProperty(key);
		if (o == null) {
			return;
		}
		ht.put(key, o);
	}

	public void start(final BundleContext context) throws Exception {
		System.out.println("GoGoEgo OSGi agent starting");
		this.context = context;
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Constants.SERVICE_PID, getName());
		context.registerService(ManagedServiceFactory.class.getName(), this,
				props);

		padmin = new ServiceTracker(context, PackageAdmin.class.getName(), null);
		padmin.open();
		cmTracker = new ServiceTracker(context, ConfigurationAdmin.class
				.getName(), null);
		cmTracker.open();

		// Created the initial configuration
		final Hashtable<String, Object> ht = new Hashtable<String, Object>();

		set(ht, DirectoryWatcher.POLL);
		set(ht, DirectoryWatcher.DIR);
		set(ht, DirectoryWatcher.DEBUG);
		updated("initial", ht);
	}

	public void stop(final BundleContext context) throws Exception {
		for (final Iterator<DirectoryWatcher> w = watchers.values().iterator(); w
				.hasNext();)
			try {
				final DirectoryWatcher dir = w.next();
				w.remove();
				dir.close();
			} catch (final Exception e) {
				// Ignore
			}
		cmTracker.close();
		padmin.close();
	}

	/**
	 * The OSGi interface specifies the unchecked Dictionary here. Sad.
	 */
	@SuppressWarnings("unchecked")
	public void updated(final String pid, final Dictionary properties)
			throws ConfigurationException {
		deleted(pid);
		final DirectoryWatcher watcher = new DirectoryWatcher(properties,
				context);
		watchers.put(pid, watcher);
		watcher.start();
	}
}
