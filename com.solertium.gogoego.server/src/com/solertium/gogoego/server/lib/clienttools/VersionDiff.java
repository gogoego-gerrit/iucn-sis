/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.gogoego.server.lib.clienttools;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.gogoego.api.utils.DocumentUtils;
import org.outerj.daisy.diff.DaisyDiff;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.xml.sax.InputSource;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.util.Replacer;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.util.restlet.MediaTypeManager;

/**
 * VersionDiff.java
 * 
 * This restlet handles version differencing backed by DaisyDiff.
 * 
 * @author carl.scott
 * 
 */
public class VersionDiff extends Restlet {

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the context
	 */
	public VersionDiff(Context context) {
		super(context);
	}

	/**
	 * Takes two versions given via query string and attempts to find and
	 * difference them, and return them in the appropriate view.
	 */
	@SuppressWarnings("deprecation")
	public void handle(Request request, Response response) {
		if (!request.getMethod().equals(Method.GET)) {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			return;
		}

		String v1 = request.getResourceRef().getQueryAsForm().getFirstValue("v1");
		String v2 = request.getResourceRef().getQueryAsForm().getFirstValue("v2");
		String url = request.getResourceRef().getRemainingPart();
		if (url.indexOf("?") > -1)
			url = url.substring(0, url.indexOf("?"));
		String view = request.getResourceRef().getQueryAsForm().getFirstValue("view");
		if (view == null || !view.equals("source"))
			view = "html";

		if (v1 == null || v2 == null || url == null) {
			setErrorResponse(response, Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}

		try {
			StringWriter writer = new StringWriter();
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

			TransformerHandler result = tf.newTransformerHandler();
			result.setResult(new StreamResult(writer));
			// result.setResult(new StreamResult(System.out));

			if (view.equals("source"))
				DaisyDiff.diffTag(getVersionAsString(url, v1, request), getVersionAsString(url, v2, request), result);
			else
				DaisyDiff.diffHTML(getVersionAsSource(url, v1, request), getVersionAsSource(url, v2, request), result,
						"", Locale.getDefault());

			String out = writer.getBuffer().toString();
			out = DocumentUtils.impl.stripDocType(out);
			// out = Replacer.replace(out, "{GGE-EL}", "${");

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("diff-html-removed", "text-decoration:line-through;color:#BBBBBB;font-style:italic;");
			map.put("diff-html-added", "background-color:lime;color:black;");
			map.put("diff-tag-removed", "text-decoration:line-through;color:#BBBBBB;font-style:italic;");
			map.put("diff-tag-added", "background-color:lime;color:black;");

			Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> cur = it.next();
				out = out.replaceAll("class=\"" + cur.getKey() + "\"", "style=\"" + cur.getValue() + "\"");
			}

			response.setEntity(new StringRepresentation(out, MediaType.TEXT_HTML));
			response.setStatus(Status.SUCCESS_OK);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			setErrorResponse(response, Status.SUCCESS_OK, e.getMessage());
		}
	}

	/**
	 * Gets a string representation of a version of a file
	 * 
	 * @param url
	 *            the url
	 * @param version
	 *            the version
	 * @return the string rep
	 */
	private String getVersionAsString(String url, String version, Request request) {
		GoGoDebug.get("fine").println("Get version as string for {0}", url);
		Request riap = new InternalRequest(request, Method.GET, "riap://host/admin/revisions" + url + "/" + version,
				null);
		Response resp = getContext().getClientDispatcher().handle(riap);

		try {
			return resp.getEntity().getText();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets a version of a file as an input source
	 * 
	 * @param url
	 *            the url
	 * @param version
	 *            the version
	 * @return the representation
	 */
	private InputSource getVersionAsSource(String url, String version, Request request) {
		Request riap = new InternalRequest(request, Method.GET, "riap://host/admin/revisions" + url + "/" + version,
				null);
		Response resp = getContext().getClientDispatcher().handle(riap);

		try {
			String str = resp.getEntity().getText();
			if (MediaTypeManager.getMediaType(url).equals(MediaType.TEXT_HTML)) {
				str = Replacer.replace(str, "&", "&amp;");
			}
			return new InputSource(new StringReader(str));
		} catch (Exception e) {
			return null;
		}
	}
	
	private void setErrorResponse(Response response, Status status) {
		setErrorResponse(response, status, null);
	}

	private void setErrorResponse(Response response, Status status, String message) {
		response.setStatus(status);
		response.setEntity(new StringRepresentation("Could not load "
				+ "file comparisons for these files due to error.  Try opening them separately or viewing in <b>Source</b> view." + (message == null ? "" : "<p>" + message + "</p>"), MediaType.TEXT_HTML));
	}

}
