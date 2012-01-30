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

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.PermissionGroupIO;
import org.iucn.sis.server.api.io.PermissionIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.TrivialExceptionHandler;

public class ProfileRestlet extends BaseServiceRestlet {

	public ProfileRestlet(final Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/profile");
		paths.add("/profile/{username}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		/**
		 * FIXME: use user IDs
		 */
		final User user = getUser(request, session);
		if (user == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		if (!user.isSISUser())
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		
		if (!SIS.amIOnline()) {
			PermissionGroupIO io = new PermissionGroupIO(session);
			PermissionGroup offline = null;
			try {
				offline = io.getPermissionGroup("offline");
			} catch (PersistentException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
			
			if (offline != null)
				user.getPermissionGroups().add(offline);
		}
		
		return new StringRepresentation(user.toFullXML(), MediaType.TEXT_XML);
	}

	@Override
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		final User user = getUser(request, session);
		if (user == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final UserIO userIO = new UserIO(session);
		if (userIO.trashUser(user.getUsername()))
			response.setStatus(Status.SUCCESS_OK);
		else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		super.handlePut(entity, request, response, session);
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
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		NativeDocument ndoc = getEntityAsNativeDocument(entity);

		User user = User.fromXML(ndoc.getDocumentElement());
		PermissionIO permissionIO = new PermissionIO(session);
		UserIO userIO = new UserIO(session);
		try {
			if (user.getPermissionGroups().isEmpty())
				user.getPermissionGroups().add(permissionIO.getPermissionGroup("guest"));
			user = (User) SIS.get().getManager().mergeObject(session, user);
			
			if (userIO.saveUser(user)) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(user.toFullXML(), MediaType.TEXT_XML);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
