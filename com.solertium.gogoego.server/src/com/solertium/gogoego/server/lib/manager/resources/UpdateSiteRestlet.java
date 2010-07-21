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

import java.io.File;

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
import org.restlet.resource.ResourceException;
import org.restlet.routing.VirtualHost;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.Bootstrap;
import com.solertium.gogoego.server.GoGoEgoVirtualHost;
import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.gogoego.server.lib.manager.html.ManagerFileLoader;
import com.solertium.util.NodeCollection;

/**
 * UpdateSiteRestlet.java
 * 
 * Provides the ability to update settings on an existing GoGoEgo site, such as
 * the resource domain regex or HTTPS settings.
 * 
 * @author carl.scott
 * 
 */
public class UpdateSiteRestlet extends Restlet {

	private final String vmroot;

	public UpdateSiteRestlet(Context context, String vmroot) {
		super(context);
		this.vmroot = vmroot;
	}

	public void handle(Request request, Response response) {
		if (request.getMethod().equals(Method.GET)) {
			try {
				handleGet(request, response);
			} catch (ResourceException e) {
				response.setStatus(e.getStatus());
			}
		} else if (request.getMethod().equals(Method.POST)) {
			try {
				handlePost(request, response);
			} catch (ResourceException e) {
				response.setStatus(e.getStatus());
			}
		}
	}

	private void handlePost(Request request, Response response) throws ResourceException {
		
		try{
			String siteID = (String) request.getAttributes().get("siteID");
			
			Document doc = new DomRepresentation(request.getEntity()).getDocument();
			Element el = (Element)doc.getDocumentElement().getElementsByTagName("host").item(0);
			String formSiteID = el.getAttribute("name");
			String match = el.getAttribute("match");
			String httpsHost = el.getAttribute("httpsHost");
			
			if (!siteID.equals(formSiteID))
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			

			final Bootstrap bs = ((ManagerApplication) Application.getCurrent()).getBootstrap();
			if (!((ManagerApplication) Application.getCurrent()).getSitesOnServer().contains(siteID))
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Can not update site with name " + siteID);
			
			if (match == null || match.equals(""))
				match = siteID + ".*";
			

			if (httpsHost == null || httpsHost.equals("") || bs.isSslSupported())
				httpsHost = null;
			
			SiteBuilder builder = new SiteBuilder();
			builder.setHostRoot(vmroot);
			builder.setSiteID(siteID);
			builder.setMatches(match);
			builder.setHttpsHost(httpsHost);

			boolean success = builder.updateSite();
			if (!success)
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, builder.getErrorMessage());

			for (VirtualHost vh : bs.getHosts()) {
				if (vh instanceof GoGoEgoVirtualHost && ((GoGoEgoVirtualHost) vh).getSiteID().equals(siteID)) {
					GoGoEgoVirtualHost site = (GoGoEgoVirtualHost) vh;
					bs.getHosts().remove(site);

					try {
						site.stop();
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not stop virtual host", e);
					}
					site.setResourceDomain(match);
					bs.getHosts().add(site);

					try {
						site.start();
						bs.getHosts().add(site);
						bs.updateHosts();
						response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
								.createConfirmDocument("Site " + siteID + " updated.")));
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not restart virtual host", e);
					}
				}

			}

		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			return;
		}

	}

	private void handleGet(Request request, Response response) throws ResourceException {
		Document hostDoc = SiteBuilder.getHostDocument(new File(vmroot + "/hosts.xml"));
		if (hostDoc == null)
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not find host document!");

		String siteID = (String) request.getAttributes().get("siteID");
		if (siteID == null || !((ManagerApplication) Application.getCurrent()).getSitesOnServer().contains(siteID))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid site ID");

		Node desired = null;
		NodeCollection nodes = new NodeCollection(hostDoc.getElementsByTagName("host"));
		for (Node node : nodes) {
			if (DocumentUtils.impl.getAttribute(node, "id").equals(siteID)) {
				desired = node;
				break;
			}
		}
		if (desired == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid site ID");

		Document document = ManagerFileLoader.fetchDocument("updatesite.html");
		if (document == null)
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not fetch document!");

		NodeCollection input = new NodeCollection(document.getElementsByTagName("input"));
		for (Node node : input) {
			if (!node.getNodeName().equals("input"))
				continue;
			String name = DocumentUtils.impl.getAttribute(node, "name");
			if (name.equals("siteID"))
				((Element) node).setAttribute("value", siteID);
			else if (name.equals("match"))
				((Element) node).setAttribute("value", DocumentUtils.impl.getAttribute(desired, "match"));
			else if (name.equals("httpsHost"))
				((Element) node).setAttribute("value", DocumentUtils.impl.getAttribute(desired, "httpsHost"));
		}

		NodeCollection forms = new NodeCollection(document.getElementsByTagName("form"));
		for (Node node : forms) {
			if (!node.getNodeName().equals("form"))
				continue;
			((Element) node).setAttribute("action", "/updateSite/" + siteID);
		}

		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(new DomRepresentation(MediaType.TEXT_HTML, document));
	}

}
