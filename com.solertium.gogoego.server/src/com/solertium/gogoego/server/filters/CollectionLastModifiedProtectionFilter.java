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

import org.gogoego.api.collections.Constants;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * CollectionLastModifiedProtectionFilter.java
 * 
 * Last modified filter implementation for collections
 * 
 * @author carl.scott
 * 
 */
public class CollectionLastModifiedProtectionFilter extends VFSLastModifiedProtectionFilter {

	/**
	 * @param context
	 * @param FILTER_KEY
	 * @param provideDiffResults
	 */
	public CollectionLastModifiedProtectionFilter(Context context, String FILTER_KEY, boolean provideDiffResults) {
		super(context, FILTER_KEY, provideDiffResults);
		eligibleMethods.add(Method.POST);
	}

	protected VFSMetadata getMetadata(Request request) {
		VFSMetadata md = null;
		VFSPath resource = getResourcePath(request);

		if (resource != null)
			md = vfs.getMetadata(resource);

		return md;
	}

	protected void setMetadata(VFSMetadata metadata, Request request) {
		VFSPath resource = getResourcePath(request);

		if (resource != null)
			try {
				vfs.setMetadata(resource, metadata);
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
	}

	protected String getVFSContent(Request request) {
		String content = null;
		VFSPath resource = getResourcePath(request);

		if (resource != null)
			try {
				content = vfs.getString(resource);
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(this, e);
			}

		return content;
	}

	private VFSPath getResourcePath(Request request) {
		VFSPath path = null;
		try {
			path = VFSResource.decodeVFSPath("/collections" + request.getResourceRef().getRemainingPart());
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		if (path == null)
			return null;

		VFSPath resource = null;
		VFSPath fileSystemLocation = new VFSPath("/(SYSTEM)" + path.toString());

		try {
			if (vfs.isCollection(fileSystemLocation))
				resource = fileSystemLocation.child(new VFSPathToken(Constants.COLLECTION_ROOT_FILENAME));
		} catch (NotFoundException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		if (resource == null) {
			VFSPath possibleXMLFile = new VFSPath(fileSystemLocation.toString() + ".xml");
			if (vfs.exists(possibleXMLFile))
				resource = possibleXMLFile;
		}

		return resource;
	}
}
