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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * AggregateActivator.java
 * 
 * Use this framework to activate multiple activators when 
 * a bundle is loaded.
 * 
 * You only need to implement addActivators, which will 
 * be called in the constructor.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class AggregateActivator implements BundleActivator {
	
	private final Collection<BundleActivator> activators;
	
	public AggregateActivator() {
		this.activators = new ArrayList<BundleActivator>();
		addActivators();
	}
	
	/**
	 * Add all your activators here.
	 */
	protected abstract void addActivators();
	
	protected void addActivator(BundleActivator activator) {
		if (!activators.contains(activator))
			this.activators.add(activator);
	}
	
	public void start(BundleContext context) throws Exception {
		for (BundleActivator activator : activators)
			activator.start(context);
	}
	
	public void stop(BundleContext context) throws Exception {
		for (BundleActivator activator : activators)
			activator.stop(context);
	}

}
