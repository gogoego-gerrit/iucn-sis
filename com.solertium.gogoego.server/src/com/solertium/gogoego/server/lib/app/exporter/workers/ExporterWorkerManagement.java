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
package com.solertium.gogoego.server.lib.app.exporter.workers;

import java.util.Collection;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ExporterWorkerManagement.java
 * 
 * Management for an ExporterWorker
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface ExporterWorkerManagement {
	
	/**
	 * Get a list of all settings required for this worker. 
	 * @return
	 */
	public Collection<String> getRequiredSettings();
	
	/**
	 * Install this worker.  Should place any necessary files on the 
	 * file system.  This is also called when someone changes any 
	 * existing settings, as there's no difference in the way most 
	 * applications will react.
	 * @param vfs
	 * @param homeFolder
	 * @param initSettings settings initially provided by the client.
	 * @throws GoGoEgoApplicationException
	 */
	public void install(VFS vfs, VFSPath homeFolder, SimpleExporterSettings initSettings) throws GoGoEgoApplicationException;
	
	/**
	 * Completely uninstall this worker.
	 * @param vfs
	 * @param homeFolder
	 * @throws GoGoEgoApplicationException
	 */
	public void uninstall(VFS vfs, VFSPath homeFolder) throws GoGoEgoApplicationException;
	
	/**
	 * Get the XML to render a UI for settings.
	 * @return
	 */
	public Document getSettingsAuthorityUI();
	
}
