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
package com.solertium.util.restlet;

import java.util.HashMap;

import org.restlet.data.Form;
import org.restlet.data.Message;
import org.restlet.data.Parameter;
import org.restlet.data.Request;

/**
 * RestletUtils.java
 *
 * @author carl.scott
 *
 */
public class RestletUtils {
	
	public static void addHeaders(final Message message, 
		final String headerName, final String headerValue) {
		Form headers = (Form) message.getAttributes().get("org.restlet.http.headers");
		if (headers == null) {
			headers = new Form();
			message.getAttributes().put("org.restlet.http.headers", headers);
		}
		headers.add(headerName, headerValue);
	}
	
	public static void setHeader(final Message message, 
			final String headerName, final String headerValue) {
			Form headers = (Form) message.getAttributes().get("org.restlet.http.headers");
			if (headers == null) {
				headers = new Form();
				message.getAttributes().put("org.restlet.http.headers", headers);
			}
			headers.removeAll(headerName);
			headers.add(headerName, headerValue);
	}

	public static String getHeader(final Message message, final String headerName) {
		String ret = null;
		try {
			final Form headers = (Form)message.getAttributes().get("org.restlet.http.headers");
			ret = headers.getFirstValue(headerName);
			if (ret == null)
				ret = headers.getFirstValue(headerName.toLowerCase());
		} catch (final Exception poorly_handled) {
			System.out.println("Restlet Header Miss: " + poorly_handled.getMessage());
		}
		return ret;
	}
	
	public static HashMap<String, String> getHeaders(final Message message) {
		final HashMap<String, String> m = new HashMap<String, String>();
		final Form headers = (Form) message.getAttributes().get("org.restlet.http.headers");
        if (headers != null)
        	for (Parameter p : headers)
        		m.put(p.getName(), p.getValue());
        return m;
	}
	
	public static String formatResourcePath(final Request request) {
		String requestURI = request.getResourceRef().getPath();
		if (requestURI.startsWith("/"))
			requestURI = requestURI.substring(1, requestURI.length());
		if (requestURI.endsWith("/"))
			requestURI = requestURI.substring(0, requestURI.length() - 1);
		return requestURI;
	}
	
	public static String[] getURIAsPieces(final Request request) {
		return formatResourcePath(request).split("/");
	}
	
	/**
	 * Returns a non-null query parameter
	 * @param request
	 * @return
	 */
	public static String getQueryParameter(final Request request, final String param) {
		String mediaType = request.getResourceRef().getQueryAsForm().getValues(param);
		if (mediaType == null)
			mediaType = "";
		return mediaType;
	}

}
