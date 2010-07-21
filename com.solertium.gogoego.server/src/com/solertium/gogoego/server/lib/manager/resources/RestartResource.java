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

import java.util.Iterator;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.routing.VirtualHost;

import com.solertium.gogoego.server.Bootstrap;
import com.solertium.gogoego.server.GoGoEgoVirtualHost;
import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;

/**
 * RestartResource.java
 * 
 * For a given site, the VirtualHost is stopped, a new instance is created, and
 * the site is re-attached to the VirtualHost list.
 * 
 * @author carl.scott
 * 
 */
public class RestartResource extends Resource {

	private final String siteID;

	public RestartResource(Context context, Request request, Response response) {
		super(context, request, response);
		siteID = (String) request.getAttributes().get("siteID");
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) throws ResourceException {
		if (!((ManagerApplication) Application.getCurrent()).getSitesOnServer().contains(siteID))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

		final Bootstrap bs = ((ManagerApplication) Application.getCurrent()).getBootstrap();
		final Iterator<VirtualHost> iterator = bs.getHosts().listIterator();

		GoGoEgoVirtualHost desiredHost = null;
		while (iterator.hasNext()) {
			VirtualHost current = iterator.next();
			if (current instanceof GoGoEgoVirtualHost && siteID.equals(((GoGoEgoVirtualHost) current).getSiteID())) {

				desiredHost = (GoGoEgoVirtualHost) current;
				break;
			}
		}

		if (desiredHost == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find host " + siteID);

		try {
			bs.getHosts().remove(desiredHost);
			desiredHost.stop();

			GoGoEgoVirtualHost vh = new GoGoEgoVirtualHost(bs.getContext().createChildContext(), desiredHost, bs
					.isSslSupported());
			bs.getHosts().add(vh);
			vh.start();
			bs.updateHosts();
		} catch (Exception e) {
			e.printStackTrace();
			return new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(e.getMessage()));
		}

		return new DomRepresentation(variant.getMediaType(), DocumentUtils.impl.createConfirmDocument(siteID
				+ " restarted."));
	}

}
