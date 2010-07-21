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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * BundleManagementBroker.java
 * 
 * @author dave.fritz
 *
 */
public class BundleManagementBroker {

	final HashMap<String, ServiceTracker> trackers;

	final BundleContext bundleContext;
	final Map<String, Bundle> cache = new HashMap<String, Bundle>();
	int count = 0;

	public BundleManagementBroker(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		trackers = new HashMap<String, ServiceTracker>();
	}

	@SuppressWarnings("unchecked")
	public Collection<Map<String, Object>> getAvailableBundles() {
		final Collection<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		final Bundle[] bundles = bundleContext.getBundles();
		
		for (Bundle bundle : bundles) {
			final Map<String, Object> map = new HashMap<String, Object>();
			final Dictionary dictionary = bundle.getHeaders();
			map.put("name", (String) dictionary.get("Bundle-Name"));
			map.put("class", (String) dictionary.get("Bundle-SymbolicName"));
			map.put("version", (String) dictionary.get("Bundle-Version"));
			map.put("provider", (String) dictionary.get("Bundle-Vendor"));

			final ServiceReference[] references = bundle.getRegisteredServices();
			ArrayList<String> services = new ArrayList<String>();
			if (references != null)
				for (ServiceReference reference : references) {
					for (String service : (String[]) reference.getProperty("objectClass"))
						services.add(service);
				}
			map.put("services", services);

			list.add(map);
		}

		return list;
	}

}
