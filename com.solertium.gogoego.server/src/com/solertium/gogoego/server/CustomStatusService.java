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
package com.solertium.gogoego.server;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.StatusService;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * CustomStatusService.java
 * 
 * Customized extension of Restlet's StatusServices that pulls 
 * the appropriate status file from the VFS when an error 
 * status is to be returned to the client.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CustomStatusService extends StatusService {

	final VFS vfs;

	public CustomStatusService(VFS vfs) {
		this.vfs = vfs;
	}

	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
		Representation ret = super.getRepresentation(status, request, response);
		VFSPath errorDocument = new VFSPath("/errors/" + status.getCode() + ".html");
		if (vfs.exists(errorDocument)) {
			Representation n = null;
			try {
				n = new StringRepresentation(vfs.getString(errorDocument), MediaType.TEXT_HTML);
			} catch (IOException io) {
				io.printStackTrace();
			}
			if (n != null)
				ret = n;
		}
		return ret;
	}

}
