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


/**
 * CoreObservable.java
 * 
 * Mimics the observable interface used in Ext without need for the Ext
 * dependency.
 * 
 */
public interface CoreObservable<T> {

	/**
	 * Adds a listener bound by the given event type.
	 * 
	 * @param eventType
	 *            the eventType
	 * @param listener
	 *            the listener to be added
	 */
	public void addListener(int eventType, T listener);

	/**
	 * Fires an event.
	 * 
	 * @param eventType
	 *            eventType the event type
	 * @return <code>true</code> if any listeners cancel the event.
	 */
	public boolean fireEvent(int eventType);

	/**
	 * Removes all listeners.
	 */
	public void removeAllListeners();

	/**
	 * Removes a listener.
	 * 
	 * @param eventType
	 *            the event type
	 * @param listener
	 *            the listener to be removed
	 */
	public void removeListener(int eventType, T listener);

}
