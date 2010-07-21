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

import java.io.IOException;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.gogoego.api.utils.DocumentUtils;
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

import com.solertium.gogoego.server.lib.app.writefilter.container.FileWritingFilterSettings;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class FileWritingFilterSettingsResource extends Resource {
	
	private static final VFSPath SETTINGS_FILE = FileWritingFilterSettings.SETTINGS_FILE;
	
	private final VFS vfs;

	public FileWritingFilterSettingsResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		vfs = GoGoEgo.get().getFromContext(context).getVFS();
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		try {
			return new GoGoEgoDomRepresentation(vfs.getDocument(SETTINGS_FILE));
		} catch (NotFoundException e) {
			return new GoGoEgoStringRepresentation("<root/>", variant.getMediaType());
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}
	
	public void storeRepresentation(Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}

		if (DocumentUtils.writeVFSFile(SETTINGS_FILE.toString(), vfs, document))
			getResponse().setStatus(Status.SUCCESS_OK);
		else
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not save");
	}

}
