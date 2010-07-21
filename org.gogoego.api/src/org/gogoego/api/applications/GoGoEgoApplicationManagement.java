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

import com.solertium.vfs.VFS;

public interface GoGoEgoApplicationManagement {
	
	/**
	 * Create directories, initialize databases, write default files, do
	 * whatever is necessary for your inInstalled() function to return true.
	 * Your application may not need to take any actions here to be installed.
	 * @param vfs TODO
	 * 
	 * @throws GoGoEgoApplicationException
	 *             if anything goes wrong in the installation process, throw
	 *             this exception and the installation will fail.
	 */
	public abstract void install(VFS vfs) throws GoGoEgoApplicationException;
	
	/**
	 * Remove files, directories, etc. directly related to this application.
	 * This method is always expected to perform a full uninstall.
	 * @param vfs TODO
	 * 
	 * @throws GoGoEgoApplicationException
	 */
	public abstract void uninstall(VFS vfs) throws GoGoEgoApplicationException;

}
