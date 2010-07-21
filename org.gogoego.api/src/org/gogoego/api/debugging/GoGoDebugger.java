/*******************************************************************************
 * Copyright (C) 2007-2009 Solertium Corporation
 * 
 * This file is part of the open source GoGoEgo project.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 *  
 * 1) The Eclipse Public License, v.1.0
 *     http://www.eclipse.org/legal/epl-v10.html
 * 
 *  2) The GNU General Public License, version 2 or later
 *     http://www.gnu.org/licenses
 ******************************************************************************/
/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package org.gogoego.api.debugging;

/**
 * GoGoDebugger.java
 * 
 * Print debugging output to the console.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface GoGoDebugger {
	
	/**
	 * Print an object via its <code>toString</code> method
	 * Consider using variable injection instead.
	 * @param toPrint
	 */
	public void println(Object toPrint);
	
	/**
	 * Print a string. Consider using variable injection 
	 * instead.
	 * @param toPrint
	 */
	public void println(String toPrint);
	
	/**
	 * Print a string, and report the class it was printed 
	 * from.  Consider using variable injection instead.
	 * @param toPrint
	 * @param clazz
	 */
	public void println(String toPrint, Class<?> clazz);

	/**
	 * Print a string, using variable injection to insert 
	 * objects that will be printed via toString().  This 
	 * is the preferred method of debugging
	 * @param toPrint 
	 * @param params
	 */
	public void println(String toPrint, Object... params);

	/**
	 * Print a string, using variable injection to insert 
	 * objects that will be printed via toString(), and 
	 * reporting what class called the debugger.	
	 * @param toPrint
	 * @param clazz
	 * @param params
	 */
	public void println(String toPrint, Class<?> clazz, Object... params);
	
	
}
