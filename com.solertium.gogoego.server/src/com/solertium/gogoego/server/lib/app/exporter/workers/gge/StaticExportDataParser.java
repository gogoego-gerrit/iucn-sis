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

import java.util.Calendar;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;

import com.solertium.gogoego.server.lib.app.exporter.workers.base.BaseExportParser;
import com.solertium.gogoego.server.lib.app.exporter.workers.base.ExportData;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * StaticExportDataParser.java
 * 
 * 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class StaticExportDataParser extends BaseExportParser {

	private final VFS vfs;
	private final Context context;

	public StaticExportDataParser(final VFS vfs, final Context context) {
		this.vfs = vfs;
		this.context = context;
	}

	public ExportData getExportData(VFSPath path) {
		ExportData exportData = null;
		try {
			exportData = new StaticExportData(path);
		} catch (InstantiationException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		return exportData;
	}

	private class StaticExportData implements ExportData {

		private Representation representation;
		private String path;

		public StaticExportData(VFSPath existingPath) throws InstantiationException {
			final Request request = new Request(Method.GET, "riap://host" + existingPath, null);

			final Response response = context.getClientDispatcher().handle(request);

			if (!response.getStatus().isSuccess())
				throw new InstantiationException("Failed: " + response.getStatus());

			representation = response.getEntity();
			path = existingPath.toString();
		}

		public Long getLastModified() {
			long lastModified;
			try {
				lastModified = representation.getModificationDate().getTime();
			} catch (NullPointerException e) {
				try {
					lastModified = vfs.getLastModified(new VFSPath(path));
				} catch (Exception f) {
					lastModified = Calendar.getInstance().getTimeInMillis();
				}
			}

			return Long.valueOf(lastModified);
		}

		public MediaType getMediaType() {
			return representation.getMediaType();
		}

		public String getPath() {
			return path;
		}

		public Representation getRepresentation() {
			return representation;
		}

		public long getSize() {
			return representation.getSize();
		}
	}

}
