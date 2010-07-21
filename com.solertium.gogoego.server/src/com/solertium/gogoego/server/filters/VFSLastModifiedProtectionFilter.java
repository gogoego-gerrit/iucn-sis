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
package com.solertium.gogoego.server.filters;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSProvidingApplication;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * VFSLastModifiedProtectionFilter.java
 * 
 * VFS implementation for last modified protection.
 * 
 * @author carl.scott
 * 
 */
public class VFSLastModifiedProtectionFilter extends LastModifiedProtectionFilter {

	protected VFS vfs;

	/**
	 * @param context
	 * @param FILTER_KEY
	 * @param provideDiffResults
	 */
	public VFSLastModifiedProtectionFilter(Context context, String FILTER_KEY, boolean provideDiffResults) {
		super(context, FILTER_KEY, provideDiffResults);
		try {
			VFSProvidingApplication app = (VFSProvidingApplication) context.getAttributes().get(
					VFSProvidingApplication.INITIALIZING_KEY);
			if (app == null)
				app = (VFSProvidingApplication) Application.getCurrent();

			this.vfs = app.getVFS();
		} catch (ClassCastException e) {
			// Maybe not the best way to handle this...
			throw new RuntimeException("Could not find a VFSProviding application!");
		}

		eligibleMethods.add(Method.PUT);
	}

	protected VFSMetadata getMetadata(Request request) {
		VFSMetadata md = null;
		VFSPath path = null;
		try {
			path = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		if (path != null && vfs.exists(path))
			md = vfs.getMetadata(path);

		return md;
	}

	protected void setMetadata(VFSMetadata metadata, Request request) {
		VFSPath path = null;
		try {
			path = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.impossible(this, e);
		}

		if (path != null && vfs.exists(path))
			try {
				vfs.setMetadata(path, metadata);
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
	}

	/**
	 * I assume that since the metadata was found (which means the path is not
	 * null and the resource exists at the given path), I can get some content
	 * from the same path.
	 */
	protected String getVFSContent(Request request) {
		String content = null;
		VFSPath path = null;
		try {
			path = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
			content = vfs.getString(path);
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.impossible(this, e);
		} catch (NotFoundException e) {
			TrivialExceptionHandler.impossible(this, e);
		} catch (BoundsException e) {
			TrivialExceptionHandler.ignore(this, e);
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		return content;
	}

}
