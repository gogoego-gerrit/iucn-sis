/*
 * Copyright (C) 2007-2008 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */

package org.iucn.sis.server.restlets.baserestlets;

import java.io.IOException;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AuthzRestlet extends ServiceRestlet {
	public AuthzRestlet(final String vfsroot, final Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/authz");
		paths.add("/authz/{username}");
		paths.add("/authz/{username}/{account}");
		paths.add("/authz/{username}/{account}/{service}");
	}

	private void doAuthorization(final Request request, final Response response, final String username)
			throws IOException {
		// String authorizeMe = request.getEntity().getText();
		final DomRepresentation rep = getPermissions(request, response, username);
		response.setEntity(rep);
		// System.out.println("Authz authorized request for " + username +
		// " to perform a " + method + " on path " + authorizeMe );

		response.setStatus(Status.SUCCESS_OK);
	}

	private DomRepresentation getPermissions(final Request request, final Response response, final String username) {
		try {
			final DomRepresentation dom = new DomRepresentation(MediaType.TEXT_XML);
			final Document doc = dom.getDocument();
			final Element rootEl = doc.createElement("rights");
			doc.appendChild(rootEl);
			rootEl.appendChild(doc.createElement("create"));
			rootEl.appendChild(doc.createElement("read"));
			rootEl.appendChild(doc.createElement("update"));
			rootEl.appendChild(doc.createElement("delete"));
			rootEl.appendChild(doc.createElement("grant"));
			rootEl.appendChild(doc.createElement("r1:createWorkingGroup"));
			return dom;
		} catch (final IOException ignore) {
		}
		return null;
	}

	@Override
	public void performService(final Request request, final Response response) {
		final ChallengeResponse creds = request.getChallengeResponse();

		/*try {
			if (creds.getIdentifier().equalsIgnoreCase("GoGoRestlet") && creds.getSecret().equals("s3cr3t"))
				System.out.println("Got a request from a Restlet!");
		} catch (final Exception ignore) {
		}*/

		try {
			final String username = (String) request.getAttributes().get("username");
			// String method = (String)request.getAttributes().get("method");

			doAuthorization(request, response, username);
		} catch (final Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
