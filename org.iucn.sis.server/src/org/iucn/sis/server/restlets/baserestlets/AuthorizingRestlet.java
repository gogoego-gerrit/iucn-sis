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

import org.iucn.sis.server.api.application.SIS;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * AuthorizingRestlet.java
 * 
 * This level of Restlet provides access to an authorize function that will
 * verify a request against a running AuthzRestlet.
 * 
 * This de-couples the VFS from the authorization process.
 * 
 * @author carl.scott
 * 
 */
public abstract class AuthorizingRestlet extends Restlet {

	public AuthorizingRestlet(final Context context) {
		super(context);
	}

	protected synchronized boolean authorize(final Request request, final Response response) {
		final String path = request.getResourceRef().getPath();
		final String method = request.getMethod().getName();
		String user = "";

		// Try to dig out the username.
		try {
			user = SIS.get().getUsername(request);
		} catch (final Exception ignored) {
			try {
				user = request.getCookies().getFirst("GoGoTicket").getName();
			} catch (final Exception ignored2) {
			}
		}

		final Request authzRequest = new Request(Method.GET, request.getResourceRef().getHostIdentifier() + "/authz/"
				+ user + "/" + method);
		authzRequest.setEntity(path, MediaType.TEXT_PLAIN);
		authzRequest.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "GoGoRestlet", "s3cr3t"));

		final Response authzResponse = new Response(authzRequest);
		getContext().getClientDispatcher().handle(authzRequest, authzResponse);

		if (authzResponse.getStatus().isSuccess())
			return true;
		else
			return false;
	}

}
