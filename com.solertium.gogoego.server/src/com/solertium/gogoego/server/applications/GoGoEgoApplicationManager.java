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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.IsPrivate;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * GoGoEgoApplicationManagr.java
 * 
 * Manages application install/uninstall.
 * 
 * @author carl.scott
 * 
 */
public class GoGoEgoApplicationManager {

	private static final VFSPath APPLICATIONS_PATH = new VFSPath("/(SYSTEM)/apps.xml");

	private final VFS vfs;

	public GoGoEgoApplicationManager(VFS vfs) {
		this.vfs = vfs;
	}

	private GoGoEgoApplicationFactory getApplicationFactory(String className) throws GoGoEgoApplicationException {
		return PluginAgent.getGoGoEgoApplicationBroker().getPlugin(className);
	}

	/**
	 * Install a particular application
	 * @param className the application
	 * @throws GoGoEgoApplicationException thrown if the application could not be installed due to error.
	 */
	public void install(String className) throws GoGoEgoApplicationException {
		install(className, new HashMap<String, String>());
	}

	/**
	 * Install a particular application with the given properties
	 * @param className the application
	 * @param properties the properties
	 * @throws GoGoEgoApplicationException thrown if the application could not be installed due to error.
	 */
	public void install(String className, HashMap<String, String> properties)
			throws GoGoEgoApplicationException {
		final GoGoEgoApplicationFactory application = getApplicationFactory(className);
		
		if (application == null)
			throw new GoGoEgoApplicationException("Application " + className + " not found.");
		
		GoGoEgoApplication instance = application.newInstance();
		if (instance instanceof IsPrivate) {
			Context context = Application.getCurrent().getContext();
			String siteID = ServerApplication.getFromContext(context).getSiteID();
			instance.init(GoGoEgo.get().getFromContext(context), className);
			if (!((IsPrivate) instance).isSiteAllowed(context, siteID)) {
				throw new GoGoEgoApplicationException("This application can not be installed on this site.");
			}
		}
				
		GoGoEgoApplicationManagement management = application.getManagement();
		if (management != null)
			management.install(vfs);

		addApplication(className, properties);
	}

	/**
	 * Uninstall an application
	 * @param className
	 * @throws GoGoEgoApplicationException
	 */
	public void uninstall(String className) throws GoGoEgoApplicationException {
		final GoGoEgoApplicationFactory application = getApplicationFactory(className);

		if (application == null)
			throw new GoGoEgoApplicationException("Application " + className + " not found.");
		
		GoGoEgoApplicationManagement management = application.getManagement();
		if (management != null)
			management.uninstall(vfs);

		removeApplication(className);
	}

	private void removeApplication(String className) {
		Document document;
		try {
			document = DocumentUtils.getReadWriteDocument(new VFSPath("/(SYSTEM)/apps.xml"), vfs);
		} catch (IOException e) {
			document = null;
		}

		if (document != null) {
			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
			for (Node node : nodes) {
				if (node.getNodeName().equals("application")
						&& DocumentUtils.impl.getAttribute(node, "bundle").equals(className)) {
					document.getDocumentElement().removeChild(node);
				}
			}
		}

		DocumentUtils.writeVFSFile(APPLICATIONS_PATH.toString(), vfs, document);
	}

	private void addApplication(String className, HashMap<String, String> properties) {
		Document document;
		try {
			document = DocumentUtils.getReadWriteDocument(new VFSPath("/(SYSTEM)/apps.xml"), vfs);
		} catch (IOException e) {
			document = null;
		}

		if (document == null) {
			document = DocumentUtils.impl.newDocument();
			document.appendChild(document.createElement("root"));
		}

		Element newChild = document.createElement("application");
		newChild.setAttribute("bundle", className);
		
		final Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<String, String> entry = iterator.next();
			Element property = document.createElement("property");
			property.setAttribute("name", entry.getKey());
			property.setTextContent(entry.getValue());

			newChild.appendChild(property);
		}

		document.getDocumentElement().appendChild(newChild);

		GoGoDebug.get("fine").println("Writing {0}", document);

		if (!DocumentUtils.writeVFSFile(APPLICATIONS_PATH.toString(), vfs, document))
			GoGoDebug.get("fine").println("Unable to write file.");

	}

	/**
	 * Perform a soft restart of the container.
	 * @param context
	 * @param siteID
	 */
	public static void restart(Context context, String siteID) {
		final String managerKey = ManagerApplication.getInternalManagerID();

		final Request req = new Request(Method.GET, 
			"riap://component/" + managerKey + "/restart/" + siteID);
		Response resp;

		GoGoDebugger debug = GoGoDebug.get("debug", siteID);
		debug.println("Attempting RIAP to {0} via component riap / server dispatch", req.getResourceRef());

		if (!(resp = context.getServerDispatcher().handle(req)).getStatus().isSuccess()) {
			debug.println("Attempting RIAP to {0} via component riap / client dispatch", req.getResourceRef());
			if (!(resp = context.getClientDispatcher().handle(req)).getStatus().isSuccess()) {
				debug.println("RIAP is a no go :(");
			} else
				try {
					debug.println("Status: {0}", resp.getStatus());
					debug.println(resp.getEntity().getText());
				} catch (IOException e) {
					debug.println("Cant print text...");
				} catch (NullPointerException e) {
					TrivialExceptionHandler.ignore(context, e);
				}
		} else
			try {
				debug.println("Status: {0}", resp.getStatus());
				debug.println(resp.getEntity().getText());
			} catch (IOException e) {
				debug.println("Cant print text...");
			}
	}
}
