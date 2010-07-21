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
package com.solertium.gogoego.server.lib.app.tags.scripting;

import java.util.ArrayList;
import java.util.Iterator;

import org.gogoego.api.scripting.ReflectingELEntity;
import org.gogoego.api.scripting.ScriptableObjectFactory;
import org.restlet.Application;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;

import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.gogoego.server.lib.app.tags.resources.TagBrowseResource;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

/**
 * TagBrowseUtility.java
 * 
 * Forwards a request to the browse resource.
 * 
 * @author carl.scott
 * 
 */
public class TagBrowseUtility implements ScriptableObjectFactory {

	public static class Worker extends ReflectingELEntity {

		private final Request request;
		private final ArrayList<String> tags;

		public Worker(Request request) {
			this.request = request;
			tags = new ArrayList<String>() {
				private static final long serialVersionUID = 1L;

				public boolean add(String e) {
					return !contains(e) && super.add(e);
				}
			};
		}

		public void addTag(String tag) {
			tags.add(tag);
		}

		public void clearTags() {
			tags.clear();
		}

		public String doBrowse(String uri) {
			return doBrowse(uri, "like");
		}

		public String doBrowse(String uri, String mode) {
			VFSPath path;
			try {
				path = VFSUtils.parseVFSPath(uri);
			} catch (VFSUtils.VFSPathParseException e) {
				return null;
			}

			String query = "?";
			final Iterator<String> iterator = tags.listIterator();
			while (iterator.hasNext())
				query += "tag=" + iterator.next() + (iterator.hasNext() ? "&" : "");

			query += (query.equals("?") ? "" : "&") + "template=" + TagBrowseResource.SEARCH_KEY
					+ (mode != null ? "&mode=" + mode : "");

			Reference reference = new Reference("riap://host/apps/"+TagApplication.REGISTRATION+"/browse/uri" + path.toString() + query);

			Request req = new InternalRequest(request, Method.GET, reference, null);
			req.getCookies().addAll(request.getCookies());

			try {
				return Application.getCurrent().getContext().getClientDispatcher().handle(req).getEntity().getText();
			} catch (Exception e) {
				return null;
			}
		}
	}

	public Object getScriptableObject(Request request) {
		return new Worker(request);
	}

}
