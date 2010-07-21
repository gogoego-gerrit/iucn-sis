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

package org.iucn.sis.server.api.restlets;

import java.util.ArrayList;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.vfs.VFS;

/**
 * This level of Restlet provides BaseRestlet functionality, and are plugged
 * into the Application by a bootstrap class.
 */
public abstract class ServiceRestlet extends VFSRestlet {

	protected ArrayList<String> paths = new ArrayList<String>();

	public ServiceRestlet(final Context context) {
		super((VFS) null, context);
		definePaths();
	}

	public ServiceRestlet(final String vfsroot, final Context context) {
		super(vfsroot, context);
		definePaths();
	}

	public ServiceRestlet(final VFS vfs, final Context context) {
		super(vfs, context);
		definePaths();
	}

	public abstract void definePaths();

	public ArrayList<String> getPaths() {
		return paths;
	}

	@Override
	public void handle(final Request request, final Response response) {
		try {
			performService(request, response);
		} catch (final Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	public abstract void performService(Request request, Response response);

	@Override
	public void setContext(final Context context) {
		super.setContext(context);
	}
}
