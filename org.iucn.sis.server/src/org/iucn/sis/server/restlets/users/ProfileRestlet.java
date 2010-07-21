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

package org.iucn.sis.server.restlets.users;

import java.io.IOException;
import java.util.HashSet;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.PermissionIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;

public class ProfileRestlet extends ServiceRestlet {

	public ProfileRestlet(final String vfsroot, final Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/profile");
		paths.add("/profile/{username}");
	}

	private void fetchUserProfile(final Request request, final Response response, final String username) {
		User user = SIS.get().getUserIO().getUserFromUsername(username);
		if (user != null) {
			if (user.isSISUser()) {
				response.setEntity(user.toXML(), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	private void deleteUserProfile(final Request request, final Response response, final String username) {

		if (SIS.get().getUserIO().trashUser(username))
			response.setStatus(Status.SUCCESS_OK);
		else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);

	}

	@Override
	public void performService(final Request request, final Response response) {
		try {
			String username = (String) request.getAttributes().get("username");

			try {
				if (username == null)
					username = SIS.get().getUsername(request);
			} catch (final Exception ignoringChallengeResponseIsNull) {
			}

			if (username != null && !username.equalsIgnoreCase("")) {
				if (request.getMethod().equals(Method.GET))
					fetchUserProfile(request, response, username);
				else if (request.getMethod().equals(Method.DELETE))
					deleteUserProfile(request, response, username);
				else if (request.getMethod().equals(Method.PUT))
					putUserProfile(request, response, username);
				else if (request.getMethod().equals(Method.POST))
					updateUserProfile(request, response, username);
				else
					response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			} else
				response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
		} catch (final Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void putUserProfile(final Request request, final Response response, final String username) {
		// SHOULD NOT DO A PUT HERE... ONLY DONE THROUGH THE SISDBAUTHENTICATOR
		// try {
		// User user = new User();
		// user.setUsername(username);
		// user.setPermissionGroups(new HashSet<PermissionGroup>());
		// user.getPermissionGroups().add(SIS.get().getPermissionIO().getPermissionGroup("guest"));
		//
		// if (SIS.get().getUserIO().saveUser(user)) {
		// response.setStatus(Status.SUCCESS_OK);
		// } else {
		// response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		// }
		// } catch (PersistentException e) {
		// e.printStackTrace();
		// response.setStatus(Status.SERVER_ERROR_INTERNAL);
		// }
	}

	private void updateUserProfile(final Request request, final Response response, final String username) {
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		try {
			ndoc.parse(request.getEntity().getText());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}

		try {
			User user = User.fromXML(ndoc.getDocumentElement());

			if (user.getPermissionGroups().isEmpty()) {
				user.getPermissionGroups().add(SIS.get().getPermissionIO().getPermissionGroup("guest"));
			}
			user = (User) SIS.get().getManager().getSession().merge(user);
			if (SIS.get().getUserIO().saveUser(user)) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(user.toXML(), MediaType.TEXT_XML);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}

	}
}
