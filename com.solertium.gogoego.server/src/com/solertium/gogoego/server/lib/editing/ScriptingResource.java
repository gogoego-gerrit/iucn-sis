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
package com.solertium.gogoego.server.lib.editing;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ScriptingResource.java
 * 
 * Return static javascript files for GoGoEgo Client
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public final class ScriptingResource extends Resource {
	
	private static final VFSPath LOCAL_SCRIPTS_PATH = new VFSPath("/private/js");
	
	private final VFS vfs;
	private final String filename;
	
	public ScriptingResource(Context context, Request request, Response response) {
		super(context, request, response);
	
		filename = request.getResourceRef().getLastSegment();
		vfs = GoGoEgo.get().getFromContext(context).getVFS();
		
		getVariants().add(new Variant(MediaType.APPLICATION_JAVASCRIPT));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		final VFSPathToken token;
		try {
			token = new VFSPathToken(filename);
		} catch (IllegalArgumentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		
		final VFSPath localPath = LOCAL_SCRIPTS_PATH.child(token);
		
		try {
			return new InputRepresentation(vfs.exists(localPath) ? 
				vfs.getInputStream(localPath) : getClass().getResourceAsStream(filename), variant.getMediaType());
		} catch (NotFoundException impossible) {
			TrivialExceptionHandler.impossible(this, impossible);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, impossible);
		}
	}

}
