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

package com.solertium.lwxml.gwt.debug;

/**
 * DebuggingApplication.java
 * 
 * Interface for an application using the SysDebugger.  Any 
 * application that wants to use SysDebugger must implement 
 * this interface.  One can use this to easily turn an entire 
 * application's debugging print statements on or off.
 * 
 * @author carl.scott@solertium.com
 *
 */
public interface DebuggingApplication {
	
	/**
	 * Returns the log level.  The SysDebugger will print anything at 
	 * the given log level and above.
	 * @return the log level
	 */
	public int getLogLevel();

}
