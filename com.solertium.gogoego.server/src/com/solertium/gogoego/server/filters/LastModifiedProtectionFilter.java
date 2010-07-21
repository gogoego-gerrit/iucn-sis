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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.gogoego.api.utils.DocumentUtils;
import org.outerj.daisy.diff.DaisyDiff;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.CookieUtility;
import com.solertium.util.restlet.RestletUtils;
import com.solertium.vfs.VFSMetadata;

/**
 * LastModifiedProtectionFilter.java
 * 
 * Checks to see if a resource has been modified since it was noted to be last
 * checked out. If so, it will present a warning.
 * 
 * @author carl.scott
 * 
 */
public abstract class LastModifiedProtectionFilter extends Filter {

	protected static final String LAST_MODIFIED_BY_PROPERTY = "lastModifiedBy";

	protected final ArrayList<Method> eligibleMethods;
	protected final String FILTER_KEY;
	protected final boolean provideDiffResults;

	public LastModifiedProtectionFilter(final Context context, final String FILTER_KEY) {
		this(context, FILTER_KEY, false);
	}

	public LastModifiedProtectionFilter(final Context context, final String FILTER_KEY, final boolean provideDiffResults) {
		super(context);
		this.FILTER_KEY = FILTER_KEY;
		this.provideDiffResults = provideDiffResults;

		eligibleMethods = new ArrayList<Method>();
	}

	protected int beforeHandle(Request request, Response response) {
		if (!eligibleMethods.contains(request.getMethod()))
			return Filter.CONTINUE;

		final String checkedOutBy = CookieUtility.associateHTTPBrowserID(request, response);
		Date checkoutDate = null;
		try {
			String header = RestletUtils.getHeader(request, FILTER_KEY);
			if (header != null)
				checkoutDate = new Date(Long.parseLong(header));
		} catch (Exception e) {
			e.printStackTrace();
			TrivialExceptionHandler.ignore(this, e);
		}

		if (checkoutDate != null && request.isEntityAvailable()) {
			VFSMetadata md = getMetadata(request);
			if (md != null) {
				try {
					Date lastModified = new Date(md.getLastModified());
					String lastModifiedBy = md.getArbitraryData().get(LAST_MODIFIED_BY_PROPERTY);

					if (lastModified.after(checkoutDate)) {
						GoGoDebug.get("debug").println("-----------");
						GoGoDebug.get("debug").println("Document being saved by {0}, last modified by " +
								"{1} is out of date", checkedOutBy, lastModifiedBy);
						GoGoDebug.get("debug").println("Document checked out on {0}", checkoutDate.getTime());
						GoGoDebug.get("debug").println("Document last modified {0}", lastModified.getTime());
						GoGoDebug.get("debug").println("-----------");
						
						String content = null;
						if (provideDiffResults) {
							try {
								content = doDiff(request);
							} catch (Exception e) {
								TrivialExceptionHandler.ignore(this, e);
							}
						}

						if (content == null)
							content = "<div><span>Last Modified:</span><span>" + lastModified.getTime()
									+ "</span></div>";

						response.setEntity(new StringRepresentation(content, MediaType.TEXT_HTML));
						response.setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);

						return Filter.STOP;
					}
				} catch (Exception e) {
					GoGoDebug.get("error").println("Last Modified Check Failed, continue; {0}", e.getMessage());
					return Filter.CONTINUE;
				}
			}
		}

		return Filter.CONTINUE;
	}

	protected void afterHandle(Request request, Response response) {
		final VFSMetadata md = getMetadata(request);
		if (md != null) {
			RestletUtils.addHeaders(response, FILTER_KEY, md.getLastModified() + "");
			if (eligibleMethods.contains(request.getMethod()) && response.getStatus().isSuccess()) {
				md.addArbitraryData(LAST_MODIFIED_BY_PROPERTY, CookieUtility.associateHTTPBrowserID(request, response));
				setMetadata(md, request);
				if (RestletUtils.getHeader(request, FILTER_KEY + "_fetch") != null) {
					if (Status.SUCCESS_NO_CONTENT.equals(response.getStatus()))
						response.setStatus(Status.SUCCESS_OK);
					response.setEntity(new DomRepresentation(MediaType.TEXT_XML, 
						DocumentUtils.impl.createConfirmDocument(Long.toString(md.getLastModified()))
					));
				}
			}
		}
	}

	protected abstract VFSMetadata getMetadata(Request request);

	protected abstract void setMetadata(VFSMetadata metadata, Request request);

	protected abstract String getVFSContent(Request request);

	protected String doDiff(Request request) throws Exception {
		StringWriter writer = new StringWriter();
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

		TransformerHandler result = tf.newTransformerHandler();
		result.setResult(new StreamResult(writer));
		// result.setResult(new StreamResult(System.out));

		DaisyDiff.diffTag(request.getEntity().getText(), getVFSContent(request), result);

		String out = writer.getBuffer().toString();
		out = stripDocType(out);
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

		return out;
	}

	private String stripDocType(String xml) {
		int index = xml.indexOf("?>");
		return (index != -1) ? xml.substring(index + 2) : xml;
	}

}
