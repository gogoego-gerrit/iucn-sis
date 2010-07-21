package org.iucn.sis.client.compiled;

import org.gogoego.api.classloader.ClassLoaderActivator;

public class Clapper extends ClassLoaderActivator {
	
	public ClassLoader getService() {
		return this.getClass().getClassLoader();
	}

}
