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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * BaseExportParserFactory.java
 * 
 * @author carl.scott
 * 
 */
public class BaseExportParserFactory {

	private final Router router;
	protected final VFS vfs;

	public BaseExportParserFactory(VFS vfs) {
		this(null, vfs);
	}

	public BaseExportParserFactory(Context context, VFS vfs) {
		this.vfs = vfs;

		router = new Router(context);
		router.attachDefault(new ParserRouteEndpoint(new VFSBaseExportParser(vfs)));
	}

	public void addParser(final String uriPattern, final BaseExportParser parser) {
		router.attach(uriPattern, new ParserRouteEndpoint(parser));
	}

	public void setDefaultParser(final BaseExportParser parser) {
		router.attachDefault(new ParserRouteEndpoint(parser));
	}

	public Map<VFSPath, ExportData> getExportData() {
		return getExportData(0);
	}

	public Map<VFSPath, ExportData> getExportData(final long date) {
		return getExportData(VFSPath.ROOT, date);
	}

	public Map<VFSPath, ExportData> getExportData(final VFSPath path, final long date) {
		final Map<VFSPath, ExportData> data = new ConcurrentHashMap<VFSPath, ExportData>();

		traverse(path, data, date);

		return data;
	}

	public List<VFSPath> findDeletedResorces(final Collection<VFSPath> paths) {
		final List<VFSPath> removedResources = new ArrayList<VFSPath>();
		for (VFSPath path : paths) {
			if (findParser(path).getExportData(path) == null)
				removedResources.add(path);
		}
		return removedResources;
	}

	private void traverse(VFSPath directory, final Map<VFSPath, ExportData> data, final long date) {
		boolean isDirectory = false;
		try {
			isDirectory = vfs.isCollection(directory);
		} catch (NotFoundException e) {
			TrivialExceptionHandler.ignore(this, e);
			return;
		}

		final VFSPathToken[] tokens;
		if (isDirectory) {
			try {
				tokens = vfs.list(directory);
			} catch (NotFoundException unlikely) {
				TrivialExceptionHandler.ignore(this, unlikely);
				return;
			}
		} else {
			tokens = new VFSPathToken[] { new VFSPathToken(directory.getName()) };
			directory = directory.getCollection();
		}

		for (VFSPathToken token : tokens) {
			final VFSPath uri = directory.child(token);
			boolean isCollection = false;
			try {
				isCollection = vfs.isCollection(uri);
			} catch (NotFoundException e) {
				TrivialExceptionHandler.ignore(this, e);
			}

			if (isCollection)
				traverse(uri, data, date);
			else {
				ExportData exportData = findParser(uri).getExportData(uri);
				if (exportData != null && (exportData.getLastModified() > date))
					data.put(uri, exportData);
			}
		}
	}

	public BaseExportParser findParser(final VFSPath path) {
		final Request request = new Request(Method.GET, new Reference(path.toString()), null);
		final Response response = new Response(request);

		Route route = (Route) router.getNext(request, response);

		return ((ParserRouteEndpoint) route.getNext()).parser;
	}

	static class ParserRouteEndpoint extends Restlet {

		private BaseExportParser parser;

		public ParserRouteEndpoint(BaseExportParser parser) {
			this.parser = parser;
		}

	}

}
