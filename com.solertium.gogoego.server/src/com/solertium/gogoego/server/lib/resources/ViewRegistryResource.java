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
package com.solertium.gogoego.server.lib.resources;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoInputRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ViewRegistryResource.java
 * 
 * GET and PUT operations for views.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ViewRegistryResource extends Resource {
	
	private static final VFSPath registry = new VFSPath("/(SYSTEM)/registry/collectiontypes.xml");
	private static final VFSPath viewFolder = new VFSPath("/(SYSTEM)/views");
	
	private final VFS vfs;
	private final String view;

	public ViewRegistryResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		vfs = GoGoEgo.get().getFromContext(context).getVFS();
		String view = request.getResourceRef().getLastSegment();
		if ("views".equals(view))
			view = null;
		this.view = view;

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		if (view == null)
			try {
				return new GoGoEgoInputRepresentation(vfs.getInputStream(registry), variant.getMediaType());
			} catch (NotFoundException e) {
				return new StringRepresentation("<root></root>", variant.getMediaType());
			}
		else {
			try {
				return new GoGoEgoInputRepresentation(vfs.getInputStream(viewFolder.child(new VFSPathToken(view))), variant.getMediaType());
			} catch (NotFoundException e) {
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
			}
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
		
		//TODO: schema verification
		
		final String writePath;
		if (view == null)
			writePath = registry.toString();
		else
			writePath = viewFolder.child(new VFSPathToken(view)).toString();
			
		if (DocumentUtils.impl.writeVFSFile(writePath, vfs, document))
			getResponse().setStatus(Status.SUCCESS_CREATED);
		else
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	}

}
