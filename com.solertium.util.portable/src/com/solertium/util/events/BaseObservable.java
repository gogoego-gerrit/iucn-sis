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
package com.solertium.util.events;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * BaseObservable.java
 * 
 * @author carl.scott
 * 
 */
public class BaseObservable implements CoreObservable<SimpleListener> {

	protected final HashMap<Integer, ArrayList<SimpleListener>> listeners;

	public BaseObservable() {
		this.listeners = new HashMap<Integer, ArrayList<SimpleListener>>();
	}

	public void addListener(int eventType, SimpleListener listener) {
		Integer key = new Integer(eventType);
		ArrayList<SimpleListener> group = listeners.get(key);
		if (group == null)
			group = new ArrayList<SimpleListener>();
		group.add(listener);
		listeners.put(key, group);
	}

	public boolean fireEvent(int eventType) {
		Integer key = new Integer(eventType);
		ArrayList<SimpleListener> group = listeners.get(key);
		boolean retValue;
		if (retValue = group != null) {
			for (SimpleListener listener : group)
				listener.handleEvent();
		}
		return retValue;
	}

	public void removeAllListeners() {
		listeners.clear();
	}

	public void removeListener(int eventType, SimpleListener listener) {
		Integer key = new Integer(eventType);
		ArrayList<SimpleListener> group = listeners.get(key);
		if (group != null)
			group.remove(listener);
	}

}
