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

package org.iucn.sis.server.baserestlets;

import java.io.Writer;
import java.util.Date;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import com.solertium.vfs.VFSPath;

public class AccountRestlet extends ServiceRestlet {

	public AccountRestlet(final String vfsroot, final Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/account");
		paths.add("/account/{user}");
	}

	private void doGet(final Request request, final Response response) {
		try {
			String user = (String) request.getAttributes().get("username");

			try {
				if (user == null)
					user = request.getChallengeResponse().getIdentifier();
			} catch (final Exception challenge) {
				response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
				response.setChallengeRequest(new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "SIS"));
				return;
			}

			if (user != null && !user.equalsIgnoreCase(""))
				fetchAccountDetails(request, response, user);
			else
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (final Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void doPut(final Request request, final Response response) {
		final String user = request.getChallengeResponse().getIdentifier();

		try {
			if (user != null && !user.equalsIgnoreCase("")) {
				if (!vfs.exists(new VFSPath("/users/" + user))) {
					// No profile. Make it first.
					final Request profileReq = new Request(Method.PUT, "/profile/" + user, request.getEntity());
					final Response profileResponse = new Response(profileReq);
					getContext().getClientDispatcher().handle(request, profileResponse);

					if (!profileResponse.getStatus().isSuccess()) {
						response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Could not create profile for user " + user
								+ ".");
						return;
					}
				}

				if (!vfs.exists(new VFSPath("/users/" + user + "/account.xml"))) {
					// Create account.
					final Writer writer = vfs.getWriter(new VFSPath("/users/" + user + "/" + "/account.xml"));
					writer.write("<account>\n<created>" + new Date().getTime() + "</created>\n</account>\n");
					writer.close();
					response.setStatus(Status.SUCCESS_CREATED);
				} else
					response.setStatus(Status.SUCCESS_OK);
			}
		} catch (final Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	public void fetchAccountDetails(final Request request, final Response response, final String user) {
		try {
			final Representation doc = new InputRepresentation(vfs.getInputStream(new VFSPath("/users/" + user + "/"
					+ "/account.xml")), MediaType.TEXT_XML);

			response.setEntity(doc);
			response.setStatus(Status.SUCCESS_OK);
		} catch (final Exception e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, user + "'s profile not found!");
		}
	}

	@Override
	public void performService(final Request request, final Response response) {
		if (!authorize(request, response)) {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}

		if (request.getMethod().equals(Method.GET))
			doGet(request, response);
		else if (request.getMethod().equals(Method.PUT))
			doPut(request, response);
	}

}
