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

import org.gogoego.api.scripting.ELEntity;

/**
 * Trap.java
 * 
 * This should be used an an API of sorts that tells page renders 
 * what functions are available to them.  This is useful for 
 * blocking off functionality and also determining when a function 
 * is called.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface Trap<T> extends ELEntity, GoGoEgoRepresentation {
	
	/**
	 * Add a touch listener
	 * @param touchListener
	 */
	public void addTouchListener(TouchListener<T> touchListener);

	/**
	 * Remove a touch listener
	 * @param touchListener
	 */
	public void removeTouchListener(TouchListener<T> touchListener);
	
	/**
	 * Fire the touch listeners
	 */
	public void touch();

}
