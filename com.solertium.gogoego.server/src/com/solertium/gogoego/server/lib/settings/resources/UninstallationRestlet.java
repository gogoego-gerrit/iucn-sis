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
package com.solertium.gogoego.server.lib.settings.resources;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.applications.GoGoEgoApplicationManager;
import com.solertium.util.NodeCollection;

/**
 * UninstallationRestlet.java
 * 
 * @author carl.scott
 * 
 */
public class UninstallationRestlet extends Restlet {

	/**
	 * @param context
	 */
	public UninstallationRestlet(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void handle(Request request, Response response) {
		try {
			uninstallApplication(request, response);
		} catch (ResourceException e) {
			response.setStatus(e.getStatus());
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(e
					.getMessage())));
		}
	}

	public void uninstallApplication(Request request, Response response) throws ResourceException {
		if (!request.getMethod().equals(Method.POST))
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

		final Document document;
		try {
			document = new DomRepresentation(request.getEntity()).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}

		GoGoDebug.get("debug").println("Received uninstall request");
		GoGoDebug.get("debug").println("- {0}", document);

		final GoGoEgoApplicationManager manager = 
			new GoGoEgoApplicationManager(ServerApplication.getFromContext(getContext()).getVFS());

		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		boolean found = false;

		for (Node node : nodes) {
			if (node.getNodeName().equals("class")) {
				String className = node.getTextContent();

				try {
					GoGoDebug.get("debug").println("Uninstalling {0}", className);
					manager.uninstall(className);
				} catch (GoGoEgoApplicationException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not install application: "
							+ e.getMessage(), e);
				}

				GoGoDebug.get("debug").println("Uninstalled {0}!", className);

				found = true;

				// One at at time for now...
				break;
			}
		}

		// Installation is successful, go for it!
		response.setStatus(Status.SUCCESS_OK);

		if (found)
			new Thread(new InstallationClass(getContext(), ServerApplication.getFromContext(getContext()).getInstanceId()))
					.start();
	}

	static class InstallationClass implements Runnable {

		private final Context context;;
		private final String siteID;

		public InstallationClass(Context context, String siteID) {
			this.context = context;
			this.siteID = siteID;
		}

		public void run() {
			GoGoDebug.get("debug", siteID).println("Restarting site {0}", siteID);
			GoGoEgoApplicationManager.restart(context, siteID);
		}

	}
}
