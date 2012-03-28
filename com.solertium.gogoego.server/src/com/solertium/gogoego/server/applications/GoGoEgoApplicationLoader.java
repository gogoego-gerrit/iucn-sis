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
package com.solertium.gogoego.server.applications;

import java.io.IOException;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.IsPrivate;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * GoGoEgoApplicationLoader.java
 * 
 * Handles loading GoGoEgo Applications
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class GoGoEgoApplicationLoader {

	private final VFS vfs;
	private final ServerApplicationImpl api;

	public GoGoEgoApplicationLoader(final ServerApplicationImpl api, final VFS vfs) {
		this.vfs = vfs;
		this.api = api;
	}

	/**
	 * Load all application
	 * @throws NotFoundException
	 */
	public void load() throws NotFoundException {
		Document doc;
		try {
			doc = DocumentUtils.getReadOnlyDocument(new VFSPath("/(SYSTEM)/apps.xml"), vfs);
		} catch (IOException e) {
			doc = null;
		}

		if (doc == null)
			throw new NotFoundException();

		final NodeCollection nodes = new NodeCollection(doc.getDocumentElement().getElementsByTagName("application"));
		for (Node node : nodes) {
			final String bundle = DocumentUtils.impl.getAttribute(node, "bundle");

			if (!bundle.equals(""))
				loadOSGiApplication(bundle, node);
		}

		onLoadComplete();
	}

	/**
	 * Reload a particular application
	 * @param registrationKey the key
	 * @throws NotFoundException thrown if the registration document could not be found.
	 */
	public void reload(final String registrationKey) throws NotFoundException {
		Document doc;
		try {
			doc = DocumentUtils.getReadOnlyDocument(new VFSPath("/(SYSTEM)/apps.xml"), vfs);
		} catch (IOException e) {
			doc = null;
		}

		if (doc == null)
			throw new NotFoundException();

		boolean found = false;
		final NodeCollection nodes = new NodeCollection(doc.getDocumentElement().getElementsByTagName("application"));
		for (Node node : nodes) {
			final String bundle = DocumentUtils.impl.getAttribute(node, "bundle");
			if (found = bundle.equals(registrationKey)) {
				loadOSGiApplication(bundle, node);
				break;
			}
		}

		if (found)
			onLoadComplete();
		else
			GoGoDebug.get("debug", api.getSiteID()).println("{0} not found, could not reload.  This is OK if this application was not already installed.", registrationKey);
	}

	private void loadOSGiApplication(final String bundleName, final Node data) {
		GoGoDebug.get("config", api.getSiteID()).println("Attempting to load bundle {0}", bundleName);
		GoGoEgoApplicationFactory factory = PluginAgent.getGoGoEgoApplicationBroker().getPlugin(bundleName);
		
		if (factory != null) {
			final GoGoEgoApplication application;
			try {
				application = factory.newInstance();
			} catch (Error e) {
				handleOSGiError(bundleName, e);
				return;
			}
			application.init(api, bundleName);
			
			if (application instanceof IsPrivate) {
				try {
					Context context = api.getContext();
					String siteID = api.getSiteID();
					
					if (!((IsPrivate) application).isSiteAllowed(context, siteID)) {
						onFailure(bundleName);
						return;
					}
				} catch (Throwable e) {
					GoGoDebug.get("error").println("Failed to update {0} on {1}: {2}", bundleName, api.getSiteID(), e.getMessage());
					onFailure(bundleName);
					return;
				}
			}

			try {
				if (application.isInstalled())
					onSuccess(application, factory.getMetaData().getName(), data);
				else
					onFailure(bundleName);
			} catch (Throwable e) {
				handleOSGiError(bundleName, e);
				return;
			}
		} else
			onFailure(bundleName);
	}
	
	private void handleOSGiError(String bundleName, Throwable e) {
		GoGoDebug.get("error", api.getSiteID()).println(
			"Application {0} threw unexpected " +
			"exception in isInstalled(): {1}\n{2}", 
			bundleName, e.getMessage(), e
		);
		onFailure(bundleName);
	}

	/**
	 * Called when an application is successfully loaded
	 * @param application
	 * @param friendlyName
	 * @param data
	 */
	public abstract void onSuccess(final GoGoEgoApplication application, final String friendlyName, final Node data);

	/**
	 * Called when an application fails to load.
	 * @param bundleName the application key
	 */
	public abstract void onFailure(final String bundleName);

	/**
	 * Stop an application's restlets
	 * @param path
	 */
	public abstract void stopApplication(final String path);

	/**
	 * Called when all applications have been loaded
	 */
	public abstract void onLoadComplete();

}
