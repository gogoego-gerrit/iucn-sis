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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.gogoego.api.plugins.GoGoEgo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;

import com.solertium.util.MD5Hash;

/**
 * A (hopefully) lighter weight implementation for the directory watcher from
 * FileInstall
 * 
 * @author Rob Heittman <rob.heittman@solertium.com>
 */
public class LightDirectoryWatcher implements Runnable {

	// Polling interval (ms)
	private final static int POLL = 5000;
	
	final static String ALIAS_KEY = "_alias_factory_pid";

	public final static String DIR = "GOGOEGO_VMROOT";

	private final boolean available;
	private final File bundleDirectory;
	private final BundleContext context;
	private final Map<String, Long> currentManagedBundles = new HashMap<String, Long>(); // location
	private final Map<String, Long> currentManagedConfigs = new HashMap<String, Long>(); // location
	private String dirLastModified = "";
	private LogService log = null;
	private boolean stopRequested = false;
	private Thread workerThread;

	@SuppressWarnings("unchecked")
	public LightDirectoryWatcher(final Dictionary properties,
			final BundleContext context) {
		this.context = context;
		String dir = (String) properties.get(DIR);
		/*FIXME
		 * This should be read from instance.ini.  But instance.ini is not
		 * read yet, so it can't.  This presents major bogosity.
		 */
		if (dir == null) dir = "workspace";
		bundleDirectory = new File(dir,"plugins");
		log(LogService.LOG_INFO, "LightDirectoryWatcher watching "
				+ bundleDirectory.getAbsolutePath());
		if (!bundleDirectory.exists()) {
			log(LogService.LOG_WARNING, "" + bundleDirectory.getAbsolutePath()
					+ " does not exist");
			available = false;
		} else if (!bundleDirectory.isDirectory()) {
			log(LogService.LOG_WARNING, "" + bundleDirectory.getAbsolutePath()
					+ " is not a directory");
			available = false;
		} else {
			available = true;
		}
	}

	/**
	 * Remove the configuration.
	 * 
	 * @param f
	 *            File where the configuration in whas defined.
	 * @return
	 * @throws Exception
	 */
	boolean deleteConfig(final File f) throws Exception {
		final String pid[] = parsePid(f.getName());
		final Configuration config = getConfiguration(pid[0], pid[1]);
		config.delete();
		return true;
	}

	/**
	 * Handle the changes between the configurations already installed and the
	 * newly found/lost configurations.
	 * 
	 * @param current
	 *            Existing installed configurations abspath -> File
	 * @param discovered
	 *            Newly found configurations
	 */
	void doConfigs(final Map<String, Long> current, final Set<String> discovered) {
		try {
			// Set all old keys as inactive, we remove them
			// when we find them to be active, will be left
			// with the inactive ones.
			final Set<String> inactive = new HashSet<String>(current.keySet());

			for (final String path : discovered) {
				final File f = new File(path);

				if (!current.containsKey(path)) {
					// newly found entry, set the config immedialey
					final Long l = new Long(f.lastModified());
					if (setConfig(f))
						// Remember it for the next round
						current.put(path, l);
				} else {
					// Found an existing one.
					// Check if it has been updated
					final long lastModified = f.lastModified();
					final long oldTime = current.get(path).longValue();
					if (oldTime < lastModified)
						if (setConfig(f))
							// Remember it for the next round.
							current.put(path, new Long(lastModified));
				}
				// Mark this one as active
				inactive.remove(path);
			}
			for (final String path : inactive) {
				final File f = new File(path);
				if (deleteConfig(f))
					current.remove(path);
			}
		} catch (final Exception ee) {
			log(LogService.LOG_INFO, "Processing config: ", ee);
		}
	}

	/**
	 * Install bundles that were discovered and uninstall bundles that are gone
	 * from the current state.
	 * 
	 * @param current
	 *            A map location -> path that holds the current state
	 * @param discovered
	 *            A set of paths that represent the just found bundles
	 */
	void doInstalled(final Map<String, Long> current,
			final Set<String> discovered) {
		boolean refresh = false;
		final Bundle bundles[] = context.getBundles();
		for (final Bundle bundle : bundles) {
			final String location = bundle.getLocation();
			if (discovered.contains(location)) {
				// We have a bundle that is already installed
				// so we know it
				discovered.remove(location);

				final File file = new File(location);

				// Modified date does not work on the Nokia
				// for some reason, so we take size into account
				// as well.
				final long newSize = file.length();
				final Long oldSizeObj = current.get(location);
				final long oldSize = oldSizeObj == null ? 0 : oldSizeObj
						.longValue();

				if (file.lastModified() > bundle.getLastModified() + 4000
						&& oldSize != newSize)
					try {
						// We treat this as an update, it is modified,,
						// different size, and it is present in the dir
						// as well as in the list of bundles.
						current.put(location, new Long(newSize));
						final InputStream in = new FileInputStream(file);
						bundle.update(in);
						refresh = true;
						in.close();
						log(LogService.LOG_INFO, "Updated " + location);
					} catch (final Exception e) {
						log(LogService.LOG_ERROR, "Failed to update bundle ", e);
					}

				// Fragments can not be started. All other
				// bundles are always started because OSGi treats this
				// as a noop when the bundle is already started
				if (!isFragment(bundle))
					try {
						bundle.start();
					} catch (final Exception e) {
						log(LogService.LOG_ERROR, "Fail to start bundle "
								+ location, e);
					}
			} else // Hmm. We found a bundle that looks like it came from our
			// watched directory but we did not find it this round.
			// Just remove it.
			if (bundle.getLocation().startsWith(
					bundleDirectory.getAbsolutePath()))
				try {
					bundle.uninstall();
					refresh = true;
					log(LogService.LOG_INFO, "Uninstalled " + location, null);
				} catch (final Exception e) {
					log(LogService.LOG_ERROR, "Failed to uninstall bundle: ", e);
				}
		}

		final List<Bundle> starters = new ArrayList<Bundle>();
		for (final String path : discovered)
			try {
				final File file = new File(path);
				final InputStream in = new FileInputStream(file);
				final Bundle bundle = context.installBundle(path, in);
				in.close();

				// We do not start this bundle yet. We wait after
				// refresh because this will minimize the disruption
				// as well as temporary unresolved errors.
				starters.add(bundle);

				log(LogService.LOG_INFO, "Installed " + file.getAbsolutePath());
			} catch (final Exception e) {
				log(LogService.LOG_ERROR, "Failed to install/start bundle: ", e);
			}

		if (refresh || starters.size() != 0) {
			refresh();
			for (final Bundle bundle : starters)
				if (!isFragment(bundle))
					try {
						bundle.start();
					} catch (final BundleException e) {
						log(
								LogService.LOG_ERROR,
								"Error while starting a newly installed bundle" + bundle.getLocation(),
								e);
					}
		}
	}

	public void examineDirectory() {
		if (!available)
			return;
		MD5Hash hash = new MD5Hash();
		for(File f : bundleDirectory.listFiles()){
			hash.update(f.getName()+"\0"+f.lastModified()+"\0"+f.length());
		}
		String fingerprint = hash.toString();
		if (!fingerprint.equals(dirLastModified)) {
			dirLastModified = fingerprint;
			GoGoEgo.debug("fine").println("New plugin directory fingerprint "+fingerprint);
			final Set<String> installed = new HashSet<String>();
			final Set<String> configs = new HashSet<String>();
			traverse(installed, configs, bundleDirectory);
			doInstalled(currentManagedBundles, installed);
			doConfigs(currentManagedConfigs, configs);
		}
	}

	Configuration getConfiguration(final String pid, final String factoryPid)
			throws Exception {
		final ConfigurationAdmin cm = (ConfigurationAdmin) PluginAgent.cmTracker
				.getService();
		if (factoryPid != null) {
			final String filter = "(|(" + ALIAS_KEY + "=" + factoryPid
					+ ")(.alias_factory_pid=" + factoryPid + "))";
			final Configuration configs[] = cm.listConfigurations(filter);
			if (configs == null || configs.length == 0)
				return cm.createFactoryConfiguration(pid, null);
			else
				return configs[0];
		} else
			return cm.getConfiguration(pid, null);
	}

	LogService getLogService() {
		if (log == null) {
			final ServiceReference ref = context
					.getServiceReference(LogService.class.getName());
			if (ref != null)
				log = (LogService) context.getService(ref);
		}
		return log;
	}

	/**
	 * Check if a bundle is a fragment.
	 * 
	 * @param bundle
	 * @return
	 */
	boolean isFragment(final Bundle bundle) {
		PackageAdmin padmin;
		if (PluginAgent.padmin == null)
			return false;

		try {
			padmin = (PackageAdmin) PluginAgent.padmin.waitForService(10000);
			if (padmin != null)
				return padmin.getBundleType(bundle) == PackageAdmin.BUNDLE_TYPE_FRAGMENT;
		} catch (final InterruptedException e) {
			// stupid exception
		}
		return false;
	}

	void log(final int logLevel, final String message) {
		log(logLevel, message, null);
	}

	/**
	 * Log a message and optional throwable. If there is a log service we use
	 * it, otherwise we log to the console
	 * 
	 * @param message
	 *            The message to log
	 * @param e
	 *            The throwable to log
	 */
	void log(final int logLevel, final String message, final Throwable e) {
		final LogService log = getLogService();
		//Eventually this will print on a site-specific level
		if (log == null)
			GoGoEgo.debug("fine").println(message + (e == null ? "" : ": " + e));
		else if (e == null)
			log.log(logLevel, message, e);
		else
			log.log(logLevel, message);
	}

	String[] parsePid(final String path) {
		String pid = path.substring(0, path.length() - 4);
		final int n = pid.indexOf('-');
		if (n > 0) {
			final String factoryPid = pid.substring(n + 1);
			pid = pid.substring(0, n);
			return new String[] { pid, factoryPid };
		} else
			return new String[] { pid, null };
	}

	/**
	 * Convenience to refresh the packages
	 */
	void refresh() {
		PackageAdmin padmin;
		try {
			padmin = (PackageAdmin) PluginAgent.padmin.waitForService(10000);
			padmin.refreshPackages(null);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void run() {
		workerThread = Thread.currentThread();
		while (!stopRequested)
			try {
				examineDirectory();
				Thread.sleep(POLL);
			} catch (final InterruptedException e) {
				// interrupt signaled
			}
	}

	/**
	 * Set the configuration based on the config file.
	 * 
	 * @param f
	 *            Configuration file
	 * @return
	 * @throws Exception
	 */
	boolean setConfig(final File f) throws Exception {
		final ConfigurationAdmin cm = (ConfigurationAdmin) PluginAgent.cmTracker
				.getService();
		if (cm == null) {
			log(LogService.LOG_ERROR,
					"Can't find a Configuration Manager, configurations do not work");
			return false;
		}

		final Properties p = new Properties();
		final InputStream in = new FileInputStream(f);
		p.load(in);
		final Hashtable<Object, Object> ht = new Hashtable<Object, Object>();
		for (final Map.Entry<Object, Object> entry : p.entrySet()) {
			final String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			value = value.trim();
			if (value.startsWith("[") && value.endsWith("]")) {
				value = value.substring(1, value.length() - 1);
				ht.put(key, value.split("\\s*,\\s*"));
			} else
				ht.put(key, value);
		}
		in.close();
		final String pid[] = parsePid(f.getName());
		if (pid[1] != null)
			ht.put(ALIAS_KEY, pid[1]);
		final Configuration config = getConfiguration(pid[0], pid[1]);
		if (config.getBundleLocation() != null)
			config.setBundleLocation(null);
		config.update(ht);
		return true;
	}

	public void stop() {
		stopRequested = true;
		workerThread.interrupt();
	}

	/**
	 * Traverse the directory and fill the map with the found jars and
	 * configurations keyed by the abs file path.
	 * 
	 * @param jars
	 *            Returns the abspath -> file for found jars
	 * @param configs
	 *            Returns the abspath -> file for found configurations
	 * @param jardir
	 *            The directory to traverse
	 */
	void traverse(final Set<String> jars, final Set<String> configs,
			final File jardir) {
		final String list[] = jardir.list();
		for (final String element : list) {
			final File file = new File(jardir, element);
			if (element.endsWith(".jar"))
				jars.add(file.getAbsolutePath());
			else if (element.endsWith(".cfg"))
				configs.add(file.getAbsolutePath());
		}
	}

}
