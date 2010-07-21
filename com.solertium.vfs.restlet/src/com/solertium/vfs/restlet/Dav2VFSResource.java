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

package com.solertium.vfs.restlet;

import net.jcip.annotations.NotThreadSafe;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;

@NotThreadSafe
public class Dav2VFSResource extends Dav1VFSResource {

	public Dav2VFSResource(final Context context, final Request request,
			final Response response) {
		super(context, request, response);
	}

	protected String getDavHeaderValue(){
		return "1,2";
	}

	public boolean allowLock() {
		return true;
	}

	public boolean allowUnlock() {
		return true;
	}

	public void handleLock() {
		// Fake locking support -- doesn't really lock anything
		final Response response = getResponse();
		final String locktoken = "" + Math.random();
		response.setEntity(new StringRepresentation(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>"
						+ "<prop xmlns=\"DAV:\">" + "<lockdiscovery>"
						+ "<activelock>" + "<locktype><write/></locktype>"
						+ "<lockscope><exclusive/></lockscope>"
						+ "<depth>0</depth>"
						+ "<owner></owner><timeout>Second-</timeout>"
						+ "<locktoken>" + "<href>opaquelocktoken:" + locktoken
						+ "</href>" + "</locktoken>" + "</activelock>"
						+ "</lockdiscovery>" + "</prop>"));
		final org.restlet.data.Form headers = (org.restlet.data.Form) response
				.getAttributes().get("org.restlet.http.headers");
		headers.add("Lock-Token", "<opaquelocktoken:" + locktoken + ">");
		response.setStatus(Status.SUCCESS_OK);
	}

	public void handleUnlock() {
		// Fake unlocking support -- doesn't really unlock anything
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}
	
	@Override
	public void handleOptions() {
		super.handleOptions();
		getResponse().getAllowedMethods().add(Method.COPY);
		getResponse().getAllowedMethods().add(Method.MOVE);
		if(VFSResource.ENTITY_HACKS)
			getResponse().setEntity(new StringRepresentation(
					"<html><body>This is an OPTIONS response detailing which" + 
					"methods are permitted to be sent from this DAV-based server." + 
					"An entity is supplied here solely to work around a Servlet" +
					"container problem; it should not be present.</body></html>",
		MediaType.TEXT_HTML));
	}

}
