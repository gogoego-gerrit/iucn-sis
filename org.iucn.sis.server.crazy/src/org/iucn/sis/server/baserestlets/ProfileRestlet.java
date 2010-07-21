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

import org.iucn.sis.server.io.ProfileIO;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.solertium.vfs.NotFoundException;

public class ProfileRestlet extends ServiceRestlet {

	public ProfileRestlet(final String vfsroot, final Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/profile");
		paths.add("/profile/{username}");
	}

	private void fetchUserProfile(final Request request, final Response response, final String user) {
		Document doc = ProfileIO.getProfileAsDocument(vfs, user);
		if( doc != null ) {
			NodeList sis = doc.getElementsByTagName("sis");
			if( sis.getLength() == 0 || sis.item(0).getTextContent().equalsIgnoreCase("true") ) {
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			}
		} else
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
	}

	private void deleteUserProfile(final Request request, final Response response, final String user) {
		try {
			if( ProfileIO.deleteProfile(vfs, user) )
				response.setStatus(Status.SUCCESS_OK);
			else
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (final NotFoundException e) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	@Override
	public void performService(final Request request, final Response response) {
		try {
			String user = (String) request.getAttributes().get("username");

			try {
				if (user == null)
					user = request.getChallengeResponse().getIdentifier();
			} catch (final Exception ignoringChallengeResponseIsNull) {
			}

			if (user != null && !user.equalsIgnoreCase("")) {
				if (request.getMethod().equals(Method.GET))
					fetchUserProfile(request, response, user);
				else if (request.getMethod().equals(Method.DELETE))
					deleteUserProfile(request, response, user);
				else if (request.getMethod().equals(Method.PUT))
					putUserProfile(request, response, user);
				else if (request.getMethod().equals(Method.POST))
					updateUserProfile(request, response, user);
				else
					response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			} else
				response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
		} catch (final Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void putUserProfile(final Request request, final Response response, final String user) {
		try {
			ProfileIO.putProfile(vfs, user);
			response.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error setting up _userTemplate.");
		}
	}

	private void updateUserProfile(final Request request, final Response response, final String user) {
		try {
			putUserProfile(request, response, user);
		} catch (Exception ignored) { 
		} //Already exists

		try {
			String payload = request.getEntity().getText();
			ProfileIO.updateUserProfile(vfs, user, payload);

			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, ProfileIO.getProfileAsDocument(vfs, user)));
			response.setStatus(Status.SUCCESS_OK);
		} catch (final Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
