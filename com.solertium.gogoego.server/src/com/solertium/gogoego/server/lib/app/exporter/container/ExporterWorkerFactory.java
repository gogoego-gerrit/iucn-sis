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
package com.solertium.gogoego.server.lib.app.exporter.container;

import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerManagement;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerMetadata;
import com.solertium.vfs.VFS;

/**
 * ExporterWorkerFactory.java
 * 
 * Create and manage workers.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface ExporterWorkerFactory {
	
	/**
	 * Create a new ExporterWorker
	 * @param vfs
	 * @return
	 */
	public ExporterWorker newInstance(VFS vfs);
	
	/**
	 * Get an instance of a manager, responsible for 
	 * installing and uninstalling a particular worker.
	 * @return
	 */
	public ExporterWorkerManagement getManagement();
	
	/**
	 * Get an accessor to metadata about this worker. 
	 * @return
	 */
	public ExporterWorkerMetadata getMetadata();

}
