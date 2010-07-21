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
package com.solertium.gogoego.server.lib.app.exporter.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterWorkerFactory;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ExportInstance.java
 * 
 * Holds general settings and a map of all configured ExporterWorkers.
 * 
 * @author carl.scott
 * 
 */
public class ExportInstance {

	private final Map<String, ExporterWorker> workers;
	private final VFS vfs;

	public ExportInstance(final VFS vfs) {
		this.workers = new ConcurrentHashMap<String, ExporterWorker>();
		this.vfs = vfs;
	}

	/**
	 * Configures ExporterWorkers from a config file
	 * 
	 * @return true if configured, false otherwise
	 */
	public boolean configure() {
		return configure(false);
	}

	/**
	 * Configures ExporterWorkers from a config file, and will fail if one of
	 * the listed exporters can not be configured if the
	 * mustInstallAllListedWorkers parameter is false.
	 * 
	 * @param mustInstallAllListedWorkers
	 * @return true if configured, false otherwise
	 */
	public boolean configure(final boolean mustInstallAllListedWorkers) {
		final HashMap<String, ExporterWorker> tempMap = new HashMap<String, ExporterWorker>();

		final Document document;
		try {
			// I need a fresh document!
			document = DocumentUtils.getReadWriteDocument(new VFSPath(ExporterConstants.CONFIG_FILE), vfs);
		} catch (IOException e) {
			return false;
		}

		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		for (Node node : nodes) {
			if (node.getNodeName().equals("exporter")) {
				final String exporterID = DocumentUtils.impl.getAttribute(node, "id");
				ExporterWorkerFactory factory = ExporterApplication.broker.getPlugin(exporterID);
				if (factory == null) {
					GoGoDebug.get("debug").println("Exporter " + exporterID + " could not be configured.");
					if (mustInstallAllListedWorkers)
						return false;
				} else {
					final VFSPath homeDirectory = new VFSPath(ExporterConstants.CONFIG_DIR).child(new VFSPathToken(exporterID));
					Document config = null;
					try {
						config = vfs.getDocument(homeDirectory.child(new VFSPathToken("config.xml")));
					} catch (NotFoundException e) {
						config = null;
					} catch (IOException e) {
						if (mustInstallAllListedWorkers)
							return false;
					}
					
					final SimpleExporterSettings settings = new SimpleExporterSettings();
					if (config != null)
						settings.loadConfig(config, factory.getManagement().getRequiredSettings());
					
					final ExporterWorker worker = factory.newInstance(vfs);
					try {
						worker.init(homeDirectory, settings);
					} catch (GoGoEgoApplicationException e) {
						if (mustInstallAllListedWorkers)
							return false;
						else
							continue;
					}
					
					GoGoDebug.get("debug").println("Exporter {0} configured successfully", exporterID);
					tempMap.put(exporterID, worker);
				}
			}
		}

		workers.clear();
		workers.putAll(tempMap);

		GoGoEgo.debug().println("Installed " + workers.size() + " exporters: " + workers.keySet());

		return true;
	}

	public boolean addWorker(final String exporterID, final ExporterWorker worker) {
		if (workers.containsKey(exporterID))
			return false;

		Document document;
		try {
			// I need a fresh document!
			document = DocumentUtils.getReadWriteDocument(new VFSPath(ExporterConstants.CONFIG_FILE), vfs);
		} catch (IOException e) {
			document = DocumentUtils.impl.newDocument();
			document.appendChild(document.createElement("root"));
		}

		final Element exporter = document.createElement("exporter");
		exporter.setAttribute("id", exporterID);
		exporter.setAttribute("enabled", "true");

		document.getDocumentElement().appendChild(exporter);

		if (DocumentUtils.writeVFSFile(ExporterConstants.CONFIG_FILE, vfs, document)) {
			updateWorker(exporterID, worker);
			return true;
		} else
			return false;
	}
	
	public void updateWorker(String key, ExporterWorker value) {
		workers.put(key, value);
	}

	public boolean removeWorker(final String exporterID) {
		if (!workers.containsKey(exporterID))
			return false;

		final Document document;
		try {
			// I need a fresh document!
			document = DocumentUtils.getReadWriteDocument(new VFSPath(ExporterConstants.CONFIG_FILE), vfs);
		} catch (IOException e) {
			return false;
		}

		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		boolean found = false;
		for (Node node : nodes) {
			if (found = (node.getNodeName().equals("exporter") && exporterID.equals(DocumentUtils.impl.getAttribute(
					node, "id")))) {
				document.getDocumentElement().removeChild(node);
				break;
			}
		}

		if (found && DocumentUtils.writeVFSFile(ExporterConstants.CONFIG_FILE, vfs, document)) {
			workers.remove(exporterID);
			return true;
		} else
			return false;
	}

	public ExporterWorker getWorker(final String exporterID) {
		return workers.get(exporterID);
	}

	public Collection<ExporterWorker> getWorkers() {
		return workers.values();
	}
	
	public Collection<String> getWorkerIDs() {
		return workers.keySet();
	}

}
