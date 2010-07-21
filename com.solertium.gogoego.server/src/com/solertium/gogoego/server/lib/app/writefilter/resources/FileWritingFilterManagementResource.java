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
package com.solertium.gogoego.server.lib.app.writefilter.resources;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.gogoego.server.lib.app.writefilter.container.FileWritingFilterSettings;

public class FileWritingFilterManagementResource extends Resource {
	
	private final String filterID;
	private final FileWritingFilterSettings settings;

	public FileWritingFilterManagementResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		filterID = (String)request.getAttributes().get("filterID");
		settings = new FileWritingFilterSettings(GoGoEgo.get().getFromContext(context).getVFS());
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public void acceptRepresentation(Representation entity) throws ResourceException {
		settings.addFilter(filterID);
		
		if (settings.save())
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		else
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	}
	
	public void removeRepresentations() throws ResourceException {
		settings.removeFilter(filterID);
		
		if (settings.save())
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		else
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	}

}
