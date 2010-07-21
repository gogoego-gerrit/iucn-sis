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
package com.solertium.gogoego.server.lib.app.exporter.container;

import java.io.IOException;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.HasSettingsUI;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.app.exporter.authority.AuthorityResource;
import com.solertium.gogoego.server.lib.app.exporter.resources.ExportResource;
import com.solertium.gogoego.server.lib.app.exporter.resources.ExporterInstallerRestlet;
import com.solertium.gogoego.server.lib.app.exporter.resources.ExporterListResource;
import com.solertium.gogoego.server.lib.app.exporter.resources.ExporterSettings;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportInstance;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExporterConstants;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ExporterApplication.java
 * 
 * Configures integrations with exporters.
 * 
 * @author carl.scott
 * 
 */
public class ExporterApplication extends GoGoEgoApplication implements GoGoEgoApplicationManagement, HasSettingsUI {

	public static final String APP_NAME = "Exporter";
	public static final String REGISTRATION = "com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication";
	
	public static final ExporterWorkerBroker broker = new ExporterWorkerBroker();

	private ExportInstance exportInstance;

	public Restlet getPublicRouter() {
		return null;
	}

	public Restlet getPrivateRouter() {
		// Impossible but...
		if (exportInstance == null)
			return null;

		Router admin = new Router(app.getContext());

		admin.attach("/list", ExporterListResource.class);
		admin.attach("/export/{exporter}/{command}", ExportResource.class);

		admin.attach("/settings", ExporterSettings.class);
		admin.attach("/settings/{worker}", ExporterSettings.class);

		admin.attach("/ui/authority/{filename}", AuthorityResource.class);

		ExporterInstallerRestlet installer = new ExporterInstallerRestlet(app.getContext(), app.getVFS());
		admin.attach("/configure/{protocol}/{exporter}", installer);

		return admin;
	}

	public String getName() {
		return APP_NAME;
	}
	
	public String getSettingsURL() {
		return "local";
	}

	public boolean isInstalled() {
		ExportInstance instance = new ExportInstance(app.getVFS());
		if (instance.configure())
			this.exportInstance = instance;
		return exportInstance != null;
	}

	public void install(VFS vfs) throws GoGoEgoApplicationException {
		try {
			vfs.makeCollections(new VFSPath(ExporterConstants.CONFIG_DIR));
		} catch (IOException e) {
			TrivialExceptionHandler.impossible(this, e);
		}

		final Document document = DocumentUtils.impl.newDocument();
		document.appendChild(document.createElement("root"));

		if (!DocumentUtils.writeVFSFile(ExporterConstants.CONFIG_FILE, vfs, document))
			throw new GoGoEgoApplicationException("Could not write config file.");
	}

	public void uninstall(VFS vfs) throws GoGoEgoApplicationException {
		try {
			vfs.delete(new VFSPath("/(SYSTEM)/exporter"));
		} catch (IOException e) {
			throw new GoGoEgoApplicationException(e);
		}
	}

	/**
	 * @return the exportInstance
	 */
	public ExportInstance getExportInstance() {
		return exportInstance;
	}

	public VFS getVFS() {
		return app.getVFS();
	}

}
