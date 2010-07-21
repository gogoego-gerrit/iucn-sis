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

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorker;
import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorkerFactory;

/**
 * SimpleSettingsAuthorityResource.java
 * 
 * Manages fetching authority files for simple settings.
 * 
 * TODO: validate authority files against schema
 * 
 * @author carl.scott
 * 
 */
public class SimpleSettingsAuthorityResource extends Resource {

	private final String remaining;
	private final SimpleSettingsWorker worker;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public SimpleSettingsAuthorityResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);

		this.remaining = (String) request.getAttributes().get("remaining");
		this.worker = SimpleSettingsWorkerFactory.getWorker((String) request.getAttributes().get("worker"), context);

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) throws ResourceException {
		if (worker == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try {
			Representation representation = worker.getAuthority(remaining);
			representation.setMediaType(variant.getMediaType());
			return representation;
		} catch (SimpleSettingsWorker.SimpleSettingsWorkerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		}
	}

}
