package org.iucn.sis.server.api.application;

import java.util.Collection;
import java.util.HashSet;

public class MultiClassLoader extends ClassLoader {
		
	private Collection<ClassLoader> loaders;
	
	public MultiClassLoader(ClassLoader parent) {
		super(parent);
		loaders = new HashSet<ClassLoader>();
	}
	
	public void addClassLoader(ClassLoader loader) {
		loaders.add(loader);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return super.loadClass(name);
		} catch (ClassNotFoundException e) {
			Class<?> cl;
			for (ClassLoader loader : loaders) {
				try {
					cl = loader.loadClass(name);
				} catch (ClassNotFoundException f) {
					continue;
				}
				if (cl != null)
					return cl;
			}
			throw e;
		}
	}
	
}
