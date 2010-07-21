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

import java.io.File;

import org.restlet.Context;

import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

/**
 * This level of Restlet provides access to a VFS and, by virtue of
 * polymorphism, an authorize function that will verify a request against a
 * running AuthzRestlet, as will be automatically started by a
 * ContainerApp-based Application.
 * 
 * @see com.solertium.baserestlets.AuthzRestlet
 * @see com.solertium.container.ContainerApp
 * 
 * @author adam
 * 
 */
public abstract class VFSRestlet extends AuthorizingRestlet {
	protected VFS vfs = null;
	protected String vfsroot = null;

	public VFSRestlet(final String vfsroot, final Context context) {
		super(context);
		this.vfsroot = vfsroot;

		initVFS();
	}

	public VFSRestlet(final VFS vfs, final Context context) {
		super(context);
		this.vfs = vfs;
	}

	private void initVFS() {
		final File spec = new File(vfsroot);
		try {
			vfs = VFSFactory.getVFS(spec);
		} catch (final NotFoundException nf) {
			throw new RuntimeException("VFS " + spec.getPath() + " could not be opened.");
		}
	}

}
