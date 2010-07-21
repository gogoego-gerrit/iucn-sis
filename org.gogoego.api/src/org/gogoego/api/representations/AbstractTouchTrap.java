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
package org.gogoego.api.representations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * AbstractTouchTrap.java
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class AbstractTouchTrap<T> implements Trap<T> {

	private final T wrapped;
	private final Collection<TouchListener<T>> touchListeners;
	
	public AbstractTouchTrap(T representation) {
		this.wrapped = representation;
		this.touchListeners = new HashSet<TouchListener<T>>();
	}
	
	public final T getWrapped() {
		touch();
		return wrapped;
	}
	
	public void addTouchListener(TouchListener<T> touchListener) {
		touchListeners.add(touchListener);
	}

	public void removeTouchListener(TouchListener<T> touchListener) {
		touchListeners.remove(touchListener);
	}
	
	public Collection<TouchListener<T>> getTouchListeners() {
		return new HashSet<TouchListener<T>>(touchListeners);
	}
	
	public final void touch() {
		for (TouchListener<T> touchListener : touchListeners) {
			touchListener.touched(wrapped);
		}
	}

}
