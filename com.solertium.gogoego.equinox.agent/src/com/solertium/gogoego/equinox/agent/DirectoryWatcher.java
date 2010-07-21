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

/**
 * -DirectoryWatcher-
 * 
 * This class runs a background task that checks a directory for new files or
 * removed files. These files can be configuration files or jars.
 */
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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;

public class DirectoryWatcher extends Thread {
	final static String ALIAS_KEY = "_alias_factory_pid";
	public final static String POLL = "com.solertium.gogoego.equinox.agent.poll";
	public final static String DIR = "com.solertium.gogoego.equinox.agent.dir";
	public final static String DEBUG = "com.solertium.gogoego.equinox.agent.debug";
	File watchedDirectory;
	long poll = 2000;
	long debug;
	BundleContext context;
	boolean reported;

	/**
	 * The unchecked Dictionary here is passed in from an OSGi interface
	 * implementation in FileInstall. Sad.
	 * 
	 * @param properties
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	public DirectoryWatcher(final Dictionary properties,
			final BundleContext context) {
		super(properties.toString());
		this.context = context;
		poll = getLong(POLL, poll);
		debug = getLong(DEBUG, -1);

		String dir = (String) properties.get(DIR);
		if (dir == null)
			dir = System.getenv(DIR);
		if (dir == null)
			dir = "./dingi";

		watchedDirectory = new File(dir);
		watchedDirectory.mkdirs();
	}

	public void close() {
		interrupt();
		try {
			join(10000);
		} catch (final InterruptedException ie) {
			// Ignore
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
					final long oldTime = (current.get(path)).longValue();
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
			log("Processing config: ", ee);
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
						log("Updated " + location, null);
					} catch (final Exception e) {
						log("Failed to update bundle ", e);
					}

				// Fragments can not be started. All other
				// bundles are always started because OSGi treats this
				// as a noop when the bundle is already started
				if (!isFragment(bundle))
					try {
						bundle.start();
					} catch (final Exception e) {
						log("Fail to start bundle " + location, e);
					}
			} else // Hmm. We found a bundlethat looks like it came from our
			// watched directory but we did not find it this round.
			// Just remove it.
			if (bundle.getLocation().startsWith(
					watchedDirectory.getAbsolutePath()))
				try {
					bundle.uninstall();
					refresh = true;
					log("Uninstalled " + location, null);
				} catch (final Exception e) {
					log("failed to uninstall bundle: ", e);
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

				log("Installed " + file.getAbsolutePath(), null);
			} catch (final Exception e) {
				log("failed to install/start bundle: ", e);
			}

		if (refresh || starters.size() != 0) {
			refresh();
			for (final Bundle bundle : starters)
				if (!isFragment(bundle))
					try {
						bundle.start();
					} catch (final BundleException e) {
						log("Error while starting a newly installed bundle" + bundle.getLocation(), e);
					}
		}
	}

	Configuration getConfiguration(final String pid, final String factoryPid)
			throws Exception {
		final ConfigurationAdmin cm = (ConfigurationAdmin) DirectoryAgent.cmTracker
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

	/**
	 * Answer the Log Service
	 * 
	 * @return
	 */
	LogService getLogService() {
		final ServiceReference ref = context
				.getServiceReference(LogService.class.getName());
		if (ref != null) {
			final LogService log = (LogService) context.getService(ref);
			return log;
		}
		return null;
	}

	/**
	 * Answer the long from a property.
	 * 
	 * @param property
	 * @param dflt
	 * @return
	 */
	long getLong(final String property, final long dflt) {
		final String value = context.getProperty(property);
		if (value != null)
			try {
				return Long.parseLong(value);
			} catch (final Exception e) {
				log(property + " set, but not a long: " + value, null);
			}
		return dflt;
	}

	/**
	 * Check if a bundle is a fragment.
	 * 
	 * @param bundle
	 * @return
	 */
	boolean isFragment(final Bundle bundle) {
		PackageAdmin padmin;
		if (DirectoryAgent.padmin == null)
			return false;

		try {
			padmin = (PackageAdmin) DirectoryAgent.padmin.waitForService(10000);
			if (padmin != null)
				return padmin.getBundleType(bundle) == PackageAdmin.BUNDLE_TYPE_FRAGMENT;
		} catch (final InterruptedException e) {
			// stupid exception
		}
		return false;
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
	void log(final String message, final Throwable e) {
		final LogService log = getLogService();
		if (log == null)
			System.out.println(message + (e == null ? "" : ": " + e));
		else if (e == null) {
			log.log(LogService.LOG_ERROR, message, e);
			if (debug > 0 && e != null)
				e.printStackTrace();
		} else
			log.log(LogService.LOG_INFO, message);
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
			padmin = (PackageAdmin) DirectoryAgent.padmin.waitForService(10000);
			padmin.refreshPackages(null);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Main run loop, will traverse the directory, and then handle the delta
	 * between installed and newly found/lost bundles and configurations.
	 * 
	 */
	@Override
	public void run() {
		log(POLL + "  (ms)   " + poll, null);
		log(DIR + "            " + watchedDirectory.getAbsolutePath(), null);
		log(DEBUG + "          " + debug, null);
		final Map<String, Long> currentManagedBundles = new HashMap<String, Long>(); // location
																						// ->
																						// Long(time)
		final Map<String, Long> currentManagedConfigs = new HashMap<String, Long>(); // location
																						// ->
																						// Long(time)

		while (!interrupted())
			try {
				final Set<String> installed = new HashSet<String>();
				final Set<String> configs = new HashSet<String>();
				traverse(installed, configs, watchedDirectory);
				doInstalled(currentManagedBundles, installed);
				doConfigs(currentManagedConfigs, configs);
				Thread.sleep(poll);
			} catch (final InterruptedException e) {
				return;
			} catch (final Throwable e) {
				log("In main loop, we have serious trouble", e);
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
		final ConfigurationAdmin cm = (ConfigurationAdmin) DirectoryAgent.cmTracker
				.getService();
		if (cm == null) {
			if (debug != 0 && !reported) {
				log(
						"Can't find a Configuration Manager, configurations do not work",
						null);
				reported = true;
			}
			return false;
		}

		final Properties p = new Properties();
		final InputStream in = new FileInputStream(f);
		p.load(in);
		final Hashtable<Object,Object> ht = new Hashtable<Object,Object>();
		for (final Map.Entry<Object,Object> entry : p.entrySet()) {
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
