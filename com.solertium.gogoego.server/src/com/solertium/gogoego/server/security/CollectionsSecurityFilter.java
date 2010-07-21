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
package com.solertium.gogoego.server.security;

import java.util.ArrayList;

import org.gogoego.api.collections.CategoryData;
import org.gogoego.api.collections.CollectionResourceBuilder;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * CollectionsSecurityFilter.java
 * 
 * Security filter extensions for collections.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionsSecurityFilter extends GoGoSecurityFilter {

	public CollectionsSecurityFilter(Context context, String key) {
		super(context, key);
		//We always want to allow collections as a public resource, leave the 
		//password protection flag though...
		warnings.remove(VFSMetadata.SECURE_REJECT_PUBLIC);
		warnings.remove(VFSMetadata.SECURE_REJECT_ALL);
	}

	public int beforeHandle(Request request, Response response) {
		VFSPath path;
		try {
			path = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
		} catch (VFSUtils.VFSPathParseException e) {
			return Filter.CONTINUE;
		}

		final String prefix = path.toString().startsWith("/collections") ? "/(SYSTEM)" : "/(SYSTEM)/collections";

		CollectionResourceBuilder b = new CollectionResourceBuilder(getContext(), false);
		CategoryData categoryData = b.getCurrentCategory(path);

		final VFSPath vfsPath;
		try {
			vfsPath = VFSUtils
					.parseVFSPath((categoryData.getItemID() == null) ? prefix + path : prefix + path + ".xml");
		} catch (Exception e) {
			// It can be handled more smartly elsewhere...
			return Filter.CONTINUE;
		}

		VFSPathToken[] split = vfsPath.getTokens();

		VFSPath cur = VFSPath.ROOT;
		for (int i = 0; i < split.length; i++) {
			cur = cur.child(split[i]);
			VFSMetadata md = vfs.getMetadata(cur);

			try {
				if (!validate(md)) {
					SecurityConditions sc = new SecurityConditions(path.toString(), md);
					sc.setFailurePath(cur.toString());
					sc.setError(new ArrayList<String>(md.getSecurityProperties().keySet()));

					response.getAttributes().put(SECURITY_FILTER, sc);

					if (isInsecure(sc, request, response) == Filter.STOP)
						return Filter.STOP;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		return Filter.CONTINUE;
	}
	
}
