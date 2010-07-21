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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

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
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.routing.VirtualHost;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.Bootstrap;
import com.solertium.gogoego.server.GoGoEgoVirtualHost;
import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.CookieUtility;

/**
 * RemoveSiteRestlet.java
 * 
 * Removes a given site from Bootstrap's list of VirtualHosts. It does not,
 * however, delete the site from the file system.
 * 
 * @author carl.scott
 * 
 */
public class RemoveSiteRestlet extends Restlet {

	private final ConcurrentHashMap<String, VerificationKey> vKeyMap = new ConcurrentHashMap<String, VerificationKey>();
	private final String vmroot;

	public RemoveSiteRestlet(Context context, String vmroot) {
		super(context);
		this.vmroot = vmroot;
	}

	public void handle(Request request, Response response) {
		if (!request.getMethod().equals(Method.GET))
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		else {
			String siteID = (String) request.getAttributes().get("siteID");
			String vKey = (String) request.getAttributes().get("vKey");

			if (siteID == null) {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a site ID");
			} else if (vKey == null) {
				if (siteID == null
						|| !((ManagerApplication) Application.getCurrent()).getSitesOnServer().contains(siteID)) {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown site " + siteID);
					return;
				}

				VerificationKey key = new VerificationKey(siteID);
				response.setEntity(new StringRepresentation("<html><head><title>Remove GoGoEgo Site</title><body>"
						+ "You have 5 minutes to remove this site.  Please visit <a href=\""
						+ request.getResourceRef().getHostIdentifier() + request.getResourceRef().getPath() + "/"
						+ key.key + "\">this link</a> to remove the site \"" + siteID + "\".</body></html>",
						MediaType.TEXT_HTML));
				response.setStatus(Status.SUCCESS_OK);
				vKeyMap.put(siteID, key);
			} else {
				VerificationKey key = vKeyMap.remove(siteID);
				if (key == null) {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Could not find site " + siteID + ".");
				} else if (!key.key.equals(vKey) || key.isExpired()) {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "You may no longer delete " + siteID
							+ ", please try again.");
				} else {
					try {
						removeSite(siteID);
						((ManagerApplication) Application.getCurrent()).getSitesOnServer().remove(siteID);
						response.setStatus(Status.SUCCESS_OK);
						response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
								.createConfirmDocument("Site " + siteID + "has been removed.")));
					} catch (ResourceException e) {
						response.setStatus(e.getStatus());
					}
				}

			}
		}
	}

	private void removeSite(String siteID) throws ResourceException {
		final File spec = new File(vmroot + "/hosts.xml");
		Document hostDoc = SiteBuilder.getHostDocument(spec);
		if (hostDoc == null)
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not find host document!");

		Node desired = null;
		NodeCollection nodes = new NodeCollection(hostDoc.getElementsByTagName("host"));
		for (Node node : nodes) {
			if (DocumentUtils.impl.getAttribute(node, "id").equals(siteID)) {
				node.getParentNode().removeChild(node);
				desired = node;
			}
		}
		if (desired == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid site ID");

		if (!SiteBuilder.writeStaticHostFile(hostDoc, spec))
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not save host document.");

		final Bootstrap bs = ((ManagerApplication) Application.getCurrent()).getBootstrap();
		for (VirtualHost vh : bs.getHosts()) {
			if (vh instanceof GoGoEgoVirtualHost) {
				GoGoEgoVirtualHost site = (GoGoEgoVirtualHost) vh;
				if (site.getSiteID().equals(siteID)) {
					try {
						site.stop();
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not stop site", e);
					}
					bs.getHosts().remove(site);
					try {
						bs.updateHosts();
					} catch (Exception e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
			}
		}
	}

	private static class VerificationKey {
		private Date expiration;
		private String key;

		public VerificationKey(String siteID) {
			this.key = CookieUtility.newUniqueID();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 5);
			this.expiration = cal.getTime();
		}

		public boolean isExpired() {
			return Calendar.getInstance().getTime().after(expiration);
		}
	}
}
