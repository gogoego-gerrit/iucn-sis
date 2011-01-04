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
package org.gogoego.api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.gogoego.api.plugins.GoGoEgo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.solertium.util.AlphanumericComparator;
import com.solertium.util.TrivialExceptionHandler;

/**
 * PluginBroker.java
 * 
 * Base class for handling a given type of OSGi plugin.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class PluginBroker<T> {

	private final BundleContext bundleContext;
	private final String trackerKey;

	private ServiceTracker tracker;
	private final Map<String, T> cache;
	private final Map<String, T> localCache;

	private final Map<String, String> serviceIDToSymbolicName;

	private final Collection<String> headerKeys;

	private int trackingCount = 0;

	public PluginBroker(BundleContext bundleContext, String trackerKey) {
		this.bundleContext = bundleContext;
		this.trackerKey = trackerKey;

		cache = new HashMap<String, T>();
		localCache = new HashMap<String, T>();
		serviceIDToSymbolicName = new HashMap<String, String>();

		headerKeys = new ArrayList<String>();

		addHeaderKey("Bundle-SymbolicName");

		try {
		tracker = new ServiceTracker(bundleContext, trackerKey, null);
		tracker.open();
		} catch (Exception e) { }

		try {
			this.bundleContext.createFilter(createFilter());
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		} catch (NullPointerException e) { }
	}

	public void addHeaderKey(String key) {
		headerKeys.add(key);
	}

	public void addLocalReference(String key, T reference) {
		localCache.put(key, reference);
	}

	@SuppressWarnings("unchecked")
	public void addListener(final PluginListener<T> listener) {
		try {
			bundleContext.addServiceListener(new ServiceListener() {
				public void serviceChanged(ServiceEvent event) {
					if (event.getType() == ServiceEvent.UNREGISTERING) {
						ServiceReference ref = event.getServiceReference();
						try {
							listener.onUnregistered(serviceIDToSymbolicName.remove(ref.getProperty("service.id")
									.toString()));
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
					} else if (event.getType() == ServiceEvent.REGISTERED || event.getType() == ServiceEvent.MODIFIED) {
						final Thread thread = new Thread(new BundleActivationWaiter(event) {
							public void onFailure() {
								GoGoEgo.debug().println("Ouch, bundle never started");
							}

							public void onSuccess(ServiceEvent event) {
								ServiceReference ref = event.getServiceReference();
								if (event.getType() == ServiceEvent.REGISTERED) {
									try {
										serviceIDToSymbolicName.put(ref.getProperty("service.id").toString(), 
												ref.getBundle().getSymbolicName());
									} catch (NullPointerException e) {
										TrivialExceptionHandler.ignore(this, e);
									}
									listener.onRegistered((T) tracker.getService(ref), ref);
								} else if (event.getType() == ServiceEvent.MODIFIED) {
									try {
										serviceIDToSymbolicName.put(ref.getProperty("service.id").toString(), 
												ref.getBundle().getSymbolicName());
									} catch (NullPointerException e) {
										TrivialExceptionHandler.ignore(this, e);
									}
									listener.onModified((T) tracker.getService(ref), ref);
								}
							}
						});
						thread.setContextClassLoader(getClass().getClassLoader());
						thread.start();
					}
				}
			}, createFilter());
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	private String createFilter() {
		return "(" + Constants.OBJECTCLASS + "=" + trackerKey + ")";
		//return "(" + trackerKey + "=*)";
	}
	
	@SuppressWarnings("unchecked")
	public T getPlugin(String classNameOrBundleName, String minimumVersion) {
		/*
		 * Local cache has no minimum version
		 */
		if (localCache.containsKey(classNameOrBundleName))
			return localCache.get(classNameOrBundleName);
		
		if (tracker == null) return null;
		
		final String compound = "" + classNameOrBundleName + "\0" + minimumVersion;
		
		if (trackingCount < tracker.getTrackingCount()) {
			trackingCount = 0;
			cache.clear();
		} else if (cache.containsKey(compound)) {
			return cache.get(compound);
		}
		
		final String mv = minimumVersion == null ? "0" : minimumVersion;
		final ServiceReference[] references = tracker.getServiceReferences();
		if (references == null)
			return null;
		
		final Map<String, ServiceReference> compatible = new HashMap<String, ServiceReference>();
		for (ServiceReference reference : references) {
			final Bundle bundle = reference.getBundle();
			if (bundle.getState() == Bundle.ACTIVE) {
				final Dictionary headers = bundle.getHeaders();
				for (String key : headerKeys) {
					if (classNameOrBundleName.equals(headers.get(key))) {
						compatible.put((String)headers.get("Bundle-Version"), reference);
						break;
					}
				}
			}
		}
		
		if (!compatible.isEmpty()) {
			final Comparator<CharSequence> ac = new VersionNumberComparator();
			final ArrayList<String> versions = new ArrayList<String>();
			versions.addAll(compatible.keySet());
			Collections.sort(versions, ac);
			for (String version : versions) {
				if (ac.compare(mv, version) <= 0) {
					T plugin = (T)tracker.getService(compatible.get(version));
					if (plugin != null) {
						cache.put(compound, plugin);
						break;
					}
				}
			}
		}
		
		return cache.get(compound);
	}

	@SuppressWarnings("unchecked")
	public T getPlugin(String classNameOrBundleName) {
		if (localCache.containsKey(classNameOrBundleName))
			return localCache.get(classNameOrBundleName);
		
		if (tracker == null) return null;

		if (trackingCount < tracker.getTrackingCount()) {
			trackingCount = 0;
			cache.clear();
		} else if (cache.containsKey(classNameOrBundleName))
			return cache.get(classNameOrBundleName);

		final ServiceReference[] references = tracker.getServiceReferences();
		if (references == null)
			return null;

		final Map<String, ServiceReference> compatible = new HashMap<String, ServiceReference>();
		for (ServiceReference reference : references) {
			final Bundle bundle = reference.getBundle();
			if (bundle.getState() == Bundle.ACTIVE) {
				final Dictionary headers = bundle.getHeaders();
				for (String key : headerKeys)
					if (classNameOrBundleName.equals(reference.getProperty(classNameOrBundleName)) || 
							classNameOrBundleName.equals((String) headers.get(key))) {
						//compatible.put(classNameOrBundleName, reference);
						compatible.put((String)headers.get("Bundle-Version"), reference);
					}
			}
		}
		
		if (!compatible.isEmpty()) {
			final Comparator<CharSequence> ac = new VersionNumberComparator();
			final ArrayList<String> versions = new ArrayList<String>();
			versions.addAll(compatible.keySet());
			
			Collections.sort(versions, Collections.reverseOrder(ac));
			
			T plugin = (T)tracker.getService(compatible.get(versions.get(0)));
			if (plugin != null) {
				cache.put(classNameOrBundleName, plugin);
				serviceIDToSymbolicName.put(compatible.get(versions.get(0)).getProperty("service.id").toString(), 
						classNameOrBundleName);
			}
		}
		
		/**
		 * This custom implementation always returns the 
		 * LATEST version of the given plugin
		 */
		/*for (ServiceReference reference : references) {
			final Bundle bundle = reference.getBundle();
			if (bundle.getState() == Bundle.ACTIVE) {
				final Dictionary headers = bundle.getHeaders();
				boolean found = false;
				for (String key : headerKeys)
					if (found = classNameOrBundleName.equals(reference.getProperty(classNameOrBundleName)) || 
							classNameOrBundleName.equals((String) headers.get(key))) {
						cache.put(classNameOrBundleName, (T) tracker.getService(reference));
						cache.put(bundle.getSymbolicName(), (T) tracker.getService(reference));
						serviceIDToSymbolicName.put(reference.getProperty("service.id").toString(), bundle
								.getSymbolicName());
						break;
					}
				if (found)
					break;
			}
		}*/

		return cache.get(classNameOrBundleName);
	}

	@SuppressWarnings("unchecked")
	public Map<String, T> getPlugins() {
		final Map<String, T> map = new HashMap<String, T>();
		map.putAll(localCache);
		
		if (tracker == null)
			return null;

		final ServiceReference[] references = tracker.getServiceReferences();
		if (references != null) {
			for (ServiceReference reference : references) {
				final Bundle bundle = reference.getBundle();
				if (bundle.getState() == Bundle.ACTIVE)
					map.put(bundle.getSymbolicName(), (T) tracker.getService(reference));
			}
		}

		return map;
	}
	
	public Map<String, Map<String, String>> getMetadata(String classNameOrBundleName) {
		final Map<String, Map<String, String>> metadata = new HashMap<String, Map<String,String>>();
		
		final ServiceReference[] references = tracker.getServiceReferences();
		if (references != null) {
			for (ServiceReference reference : references) {
				Bundle bundle = reference.getBundle();
				if (bundle != null && classNameOrBundleName.equals(bundle.getSymbolicName())) {
					final Dictionary dictionary = bundle.getHeaders();
					final Map<String, String> headers = new HashMap<String, String>() {
						private static final long serialVersionUID = 1L;
						public String put(String key, String value) {
							if (value == null) value = "N/A";
							return super.put(key, value);
						}
					};
					headers.put("Bundle-Version", (String)dictionary.get("Bundle-Version"));
					headers.put("Bundle-Vendor", (String)dictionary.get("Bundle-Vendor"));
					
					metadata.put(headers.get("Bundle-Version"), headers);
				}
			}
		}
		
		return metadata;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> getMetadata() {
		final Map<String, Map<String, String>> metadata = new HashMap<String, Map<String,String>>();
		
		final Map<String, String> localMetadata = new HashMap<String, String>();
		localMetadata.put("Bundle-Version", "1.1.0");
		localMetadata.put("Bundle-Vendor", "Solertium");
		for (String key : localCache.keySet()) {
			metadata.put(key, localMetadata);
		}
		
		final ServiceReference[] references = tracker.getServiceReferences();
		if (references != null) {
			for (ServiceReference reference : references) {
				Bundle bundle = reference.getBundle();
				if (bundle != null) {
					final Dictionary dictionary = bundle.getHeaders();
					final Map<String, String> headers = new HashMap<String, String>() {
						private static final long serialVersionUID = 1L;
						public String put(String key, String value) {
							if (value == null) value = "N/A";
							return super.put(key, value);
						}
					};
					headers.put("Bundle-Version", (String)dictionary.get("Bundle-Version"));
					headers.put("Bundle-Vendor", (String)dictionary.get("Bundle-Vendor"));
					
					metadata.put(bundle.getSymbolicName(), headers);
				}
			}
		}
		
		return metadata;
	}

	public void release(String classNameOrBundleName) {
		cache.remove(classNameOrBundleName);
	}

	public static class PluginListener<T> {
		public void onRegistered(T service, ServiceReference reference) {
		}

		public void onModified(T service, ServiceReference reference) {
		}

		public void onUnregistered(String symbolicName) {
		}
	}

	private static abstract class BundleActivationWaiter implements Runnable {

		private final ServiceEvent event;

		public BundleActivationWaiter(ServiceEvent event) {
			this.event = event;
		}

		public void run() {
			if (event.getServiceReference().getBundle().getState() == Bundle.STARTING) {
				while (event.getServiceReference().getBundle().getState() == Bundle.STARTING) {
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
						break;
					}
				}
				finish();
			} else
				finish();
		}

		private void finish() {
			if (event.getServiceReference().getBundle().getState() == Bundle.ACTIVE)
				onSuccess(event);
			else
				onFailure();
		}

		public abstract void onSuccess(ServiceEvent event);

		public abstract void onFailure();
	}
	
	public static class VersionNumberComparator implements Comparator<CharSequence> {
		
		private final AlphanumericComparator comparator = new AlphanumericComparator();
		
		public int compare(CharSequence o1, CharSequence o2) {
			String[] a = o1.toString().split("\\.");
			String[] b = o2.toString().split("\\.");
			
			for (int i = 0; i < a.length; i++) {
				String curA = a[i];
				String curB;
				try {
					curB = b[i];
				} catch (IndexOutOfBoundsException e) {
					return 1;
				}
				
				if (curA.equals(curB))
					continue;
				
				return comparator.compare(curA, curB);
			}
			
			return 0;
		}
		
	}

}
