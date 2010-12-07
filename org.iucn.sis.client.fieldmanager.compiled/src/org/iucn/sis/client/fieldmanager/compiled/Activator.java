package org.iucn.sis.client.fieldmanager.compiled;

import org.gogoego.api.classloader.ClassLoaderActivator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends ClassLoaderActivator {

	@Override
	public ClassLoader getService() {
		return getClass().getClassLoader();
	}

}
