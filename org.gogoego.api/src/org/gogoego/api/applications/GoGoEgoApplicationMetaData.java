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
package org.gogoego.api.applications;


/**
 * GoGoEgoApplicationMetaData.java
 * 
 * Serves general metadata about an application independent of 
 * its implementation.
 * 
 * @author liz.schwartz
 * 
 * Added name to the metadata functionality.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface GoGoEgoApplicationMetaData {
	
	/**
	 * The friendly name of the application, to be displayed in UI 
	 * components and logging, when available.
	 * 
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Returns the description of the application to be displayed in 
	 * settings tab
	 * 
	 * @return
	 */
	public String getDescription();
	
}
