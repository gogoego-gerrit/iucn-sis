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

package com.solertium.gogoego.server.connectors;

import java.util.ArrayList;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

/**
 * FCKRedirectRestlet.java
 * 
 * Wraps the clap cache directory for ForkEditor. You can supply a list of
 * supported paths, and the handle method will check to see if the given path is
 * in the set. If, so, then it will check the VFS and the public directory for
 * the particular file. That failing, the normal directory lookup will be called
 * to handle the request.
 * 
 * @author carl.scott
 * 
 */
public class FCKRedirectRestlet extends Filter {

	private static final String FCK_USER_PATH = "/(SYSTEM)/fck";
	private static final String FCK_CLIENT_PATH = "/admin/fck";

	private ArrayList<String> supportedPaths;
	private final VFS vfs;

	private String baseUrl = "";

	public FCKRedirectRestlet(Context context) {
		super(context);
		supportedPaths = new ArrayList<String>();

		vfs = ServerApplication.getFromContext(context).getVFS();
	}

	/**
	 * Adds a path to the list of supported paths, given a RELATIVE url
	 * 
	 * @param path
	 *            the URL relative to the submitted baseUrl of the ForkEditor
	 *            directory structure
	 */
	public void addSupportedPath(String path) {
		supportedPaths.add(baseUrl + path);
	}

	/**
	 * Sets the base url of the ForkEditor
	 * 
	 * @param baseUrl
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	@Override
	protected int beforeHandle(Request request, Response response) {
		if (request.getMethod().equals(Method.GET) && supportedPaths.contains(request.getResourceRef().getPath())) {
			String fileName = request.getResourceRef().getPath().replaceFirst(baseUrl, "");
			final VFSPath path;
			try {
				path = VFSUtils.parseVFSPath(FCK_USER_PATH + fileName);
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
				return Filter.STOP;
			}

			if (vfs.exists(path)) {
				Request fetch = new InternalRequest(request, request.getMethod(), "riap://host/admin/files" + FCK_USER_PATH
						+ fileName, request.getEntity());
				Response fetchResp = getContext().getClientDispatcher().handle(fetch);
				GoGoEgo.debug().println("{0} is unique to site? {1}", fileName, fetchResp.getStatus());
				if (fetchResp.getStatus().isSuccess() || fetchResp.getStatus().isRedirection()) {
					response.setStatus(fetchResp.getStatus());
					response.setEntity(fetchResp.getEntity());
				}
			} else {
				Request fetch = new InternalRequest(request, request.getMethod(), "riap://host" + FCK_CLIENT_PATH
						+ fileName, request.getEntity());
				Response fetchResp = getContext().getClientDispatcher().handle(fetch);
				GoGoEgo.debug().println("{0} is implemented in client tool? {1}", fileName, fetchResp.getStatus());
				if (fetchResp.getStatus().isSuccess() || fetchResp.getStatus().isRedirection()) {
					response.setStatus(fetchResp.getStatus());
					response.setEntity(fetchResp.getEntity());
				} else {
					return Filter.CONTINUE;
				}
			}
		} 
		return Filter.CONTINUE;
	}

}
