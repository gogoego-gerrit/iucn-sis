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
package com.solertium.gogoego.server.lib.app.exporter.workers.base;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * VFSBaseExportParser.java
 * 
 * @author carl.scott
 * 
 */
public class VFSBaseExportParser extends BaseExportParser {

	private final VFS vfs;

	public VFSBaseExportParser(final VFS vfs) {
		this.vfs = vfs;
	}

	public ExportData getExportData(VFSPath path) {
		ExportData exportData = null;
		try {
			if (vfs.exists(path))
				exportData = new VFSExportData(path);
		} catch (InstantiationException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		return exportData;
	}

	class VFSExportData implements ExportData {

		private long lastModified;
		private Representation representation;
		private MediaType mediaType;
		private String path;
		private long size;

		public VFSExportData(VFSPath existingPath) throws InstantiationException {
			path = existingPath.toString();

			try {
				size = vfs.getLength(existingPath);
				lastModified = vfs.getLastModified(existingPath);
			} catch (NotFoundException e) {
				throw new InstantiationException(e.getMessage());
			}

			mediaType = MediaTypeManager.getMediaType(existingPath.toString());

			try {
				representation = new InputRepresentation(vfs.getInputStream(existingPath), mediaType);
				representation.setSize(size);
			} catch (IOException e) {
				debug().println("# InputRepresentation failure for " + existingPath);
				throw new InstantiationException(e.getMessage());
			}
		}

		public Long getLastModified() {
			return Long.valueOf(lastModified);
		}

		public MediaType getMediaType() {
			return mediaType;
		}

		public String getPath() {
			return path;
		}

		public long getSize() {
			return size;
		}

		public String toString() {
			return "Modified " + lastModified + ", type = " + mediaType;
		}

		public Representation getRepresentation() {
			return representation;
		}
	}

}
