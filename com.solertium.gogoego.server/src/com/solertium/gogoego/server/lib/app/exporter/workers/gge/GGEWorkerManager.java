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
package com.solertium.gogoego.server.lib.app.exporter.workers.gge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerManagement;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerMetadata;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * GGEWorkerManager.java
 * 
 * Manager and metadata for GGE-to-GGE exporter.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class GGEWorkerManager implements ExporterWorkerManagement, ExporterWorkerMetadata {
	
	public Collection<String> getRequiredSettings() {
		final ArrayList<String> list = new ArrayList<String>();
		list.add("location");
		list.add("key");
		return list;
	}
	
	public Document getSettingsAuthorityUI() {
		return BaseDocumentUtils.impl.getInputStreamFile(getClass().getResourceAsStream("gge.xml"));
	}
	
	public void install(VFS vfs, VFSPath folder, SimpleExporterSettings initSettings) throws GoGoEgoApplicationException {
		if (!vfs.exists(folder))
			try {
				vfs.makeCollections(folder);
			} catch (IOException e) {
				throw new GoGoEgoApplicationException("Could not make necessary directories.");
			}
			
		DocumentUtils.writeVFSFile(
			folder.child(new VFSPathToken("config.xml")).toString(), vfs,
			DocumentUtils.impl.createDocumentFromString(initSettings.toXML()));
	}
	
	public void uninstall(VFS vfs, VFSPath folder) throws GoGoEgoApplicationException {
		try {
			vfs.delete(folder);
		} catch (NotFoundException e) {
			throw new GoGoEgoApplicationException(e);
		} catch (ConflictException e) {
			throw new GoGoEgoApplicationException(e);
		}
	}
	
	public String getDescription() {
		return "Export your GoGoEgo site to another GoGoEgo Site.";
	}
	
	public String getName() {
		return "GoGoEgo Exporter";
	}

}
