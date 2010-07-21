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
 * GoGoEgoApplicationFactory.java
 * 
 * A factory method that allows you to create and manage GoGoEgoApplications.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface GoGoEgoApplicationFactory {
	
	/**
	 * Get the management object that allows this application 
	 * to be installed and uninstalled.  This is needed when 
	 * your application needs to write information to the 
	 * file system first BEFORE your application attempts to 
	 * load (i.e. before GoGoEgoApplication.isInstalled()) is 
	 * called, and when you need to remove files from the 
	 * file system when your application is to be uninstalled. 
	 * @return the manager, or null if you do not 
	 * need this functionality
	 */
	public GoGoEgoApplicationManagement getManagement();
	
	/**
	 * Returns a fresh new instance of a GoGoEgoApplication 
	 * each time this is called.
	 * @return new application
	 */
	public GoGoEgoApplication newInstance();
	
	
	/**
	 * Get the metadata associated with a GoGoEgoApplication
	 * @return
	 */
	public GoGoEgoApplicationMetaData getMetaData();

}
