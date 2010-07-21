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
package com.solertium.gogoego.server.lib.manager.resources;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.routing.VirtualHost;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.Bootstrap;
import com.solertium.gogoego.server.GoGoEgoVirtualHost;
import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.gogoego.server.lib.manager.html.ManagerFileLoader;

/**
 * AttachSiteRestlet.java
 * 
 * Adds a new site to a running Bootstrap Component
 * 
 * @author carl.scott
 * 
 */
public class AttachSiteRestlet extends Restlet {

	private final String vmroot;

	public AttachSiteRestlet(Context context, String vmroot) {
		super(context);
		this.vmroot = vmroot;
	}

	public void handle(Request request, Response response) {
		if (request.getMethod().equals(Method.GET)) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(ManagerFileLoader.fetchRepresentation("addsite.html"));
		} else if (request.getMethod().equals(Method.POST)) {
			try{
				Document doc = new DomRepresentation(request.getEntity()).getDocument();
				Element el = (Element)doc.getDocumentElement().getElementsByTagName("host").item(0);
				String siteId = el.getAttribute("name");
				String match = el.getAttribute("match");
				String httpsHost = el.getAttribute("httpsHost");
				
				createSite(siteId, match, httpsHost,
						response);
			} catch (Exception e) {
				response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
				return;
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			return;
		}
	}

	private void createSite(String siteID, String matches, String httpsHost, Response response) {
		final Bootstrap bs = ((ManagerApplication) Application.getCurrent()).getBootstrap();
		if (siteID == null || ((ManagerApplication) Application.getCurrent()).getSitesOnServer().contains(siteID)) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
					.createErrorDocument("Can not create site with name " + siteID)));
			return;
		}
		if (matches == null)
			matches = siteID + ".*";
		if (httpsHost == null || httpsHost.equals("") || bs.isSslSupported())
			httpsHost = null;

		final SiteBuilder x = new SiteBuilder();
		x.setHostRoot(vmroot);
		x.setSiteID(siteID);
		x.setMatches(matches);
		x.setHttpsHost(httpsHost);

		boolean success = x.createNewSite();
		if (!success) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(x
					.getErrorMessage())));
		} else {
			Element hostNode = DocumentUtils.impl.newDocument().createElement("host");
			hostNode.setAttribute("id", siteID);
			hostNode.setAttribute("match", matches);
			hostNode.setAttribute("httpsHost", httpsHost);

			VirtualHost vh = new GoGoEgoVirtualHost(bs.getContext(), bs.isSslSupported(), vmroot, hostNode);
			bs.getHosts().add(vh);

			try {
				vh.start();
				bs.updateHosts();
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
						.createConfirmDocument("New Site " + siteID + " created.")));

				((ManagerApplication) Application.getCurrent()).getSitesOnServer().add(siteID);
			} catch (Exception e) {
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
						.createErrorDocument("Could not start new site: " + e.getMessage())));
				e.printStackTrace();
				x.killSite();
			}
		}
	}

}
