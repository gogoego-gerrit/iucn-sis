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
package com.solertium.gogoego.server.lib.app.publishing.resources;

import org.gogoego.api.representations.GoGoEgoStringRepresentation;
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
import org.w3c.dom.Document;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.publishing.container.CollectionPublishingSettings;
import com.solertium.gogoego.server.lib.app.publishing.container.PublishingApplication;

/**
 * ImportSettingsResource.java
 * 
 * TODO: save settings
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionPublishingSettingsResource extends Resource {
	
	private final CollectionPublishingSettings settings;

	public CollectionPublishingSettingsResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		settings = ((PublishingApplication)ServerApplication.
			getFromContext(context, PublishingApplication.REGISTRATION)).getSettings();

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		return new GoGoEgoStringRepresentation(settings.toXML(), variant.getMediaType());
	}
	
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument(); 
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		settings.load(document);
	}

}
