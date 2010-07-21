/*
 * Copyright (C) 2007-2009 Solertium Corporation
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
package com.solertium.gogoego.server.filters;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.resources.StaticPageTreeNode;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.RestletUtils;

/**
 * ConditionalGetFilter.java
 * 
 * This filter captures requests for conditional GETs and 
 * checks the pagetree for text/html content to determine 
 * if it has been modified since the header date provided. 
 * If so, it will 304; otherwise, it will continue as 
 * normal and fetch the resource.
 * 
 * For all other types of content, it will act on the browser's 
 * natural instincts, so 304s will occur as expected.  In 
 * most of these cases, it will be looking for a VFSResource, 
 * in which case the last modified date check will be correctly 
 * processed.
 * 
 * @author carl.scott
 *
 */
public class ConditionalGetFilter extends Filter {
	
	private static final DateFormat normalizer = DateFormat.getDateTimeInstance();
	
	public ConditionalGetFilter(Context context, Restlet next) {
		super(context, next);
	}
	
	
	@SuppressWarnings("deprecation")
	protected int beforeHandle(Request request, Response response) {
		final Map<String, StaticPageTreeNode> map = ServerApplication.getFromContext(getContext()).getLastModifiedMap();
		
		//Do I even care about this request?
		if (!(Protocol.HTTP.equals(request.getProtocol()) || Protocol.HTTPS.equals(request.getProtocol())) || !Method.GET.equals(request.getMethod()))
			return Filter.CONTINUE;
		//Throw a query string on the request, just resubmit, you're probably trying to send a command anyway 
		else if (request.getResourceRef().getQuery() != null) {
			stripHeader(request);
			return Filter.CONTINUE;
		}
		else {
			/*
			 * Check if server has rendered the page.  If NOT, then strip 
			 * the header and do a real GET ... this overrides the browser 
			 * cache behavior but hey, you're already hitting the server so, meh. 
			 */
			final StaticPageTreeNode node = map.get(request.getResourceRef().getPath());
			if (node == null) {
				stripHeader(request);
				return Filter.CONTINUE;
			}
			
			/*
			 * This information may have already expired...
			 */
			final Date expiration = node.getExpirationDate();
			if (expiration != null) {
				final Date rightNow = Calendar.getInstance().getTime();
				if (expiration.after(rightNow)) {
					stripHeader(request);
					GoGoEgo.getCacheHandler().removeFromCache(getContext(), request.getResourceRef().getPath());
					return Filter.CONTINUE;
				}
			}
			
			final String modifiedDate = RestletUtils.getHeader(request, "If-Modified-Since");
			if (modifiedDate ==  null)
				return Filter.CONTINUE;
			
			//FIXME: this could break in the future, I guess...
			final Date date;
			try {
				date = new Date(modifiedDate);
			} catch (Exception e) {
				return Filter.CONTINUE;
			}
			
			final Date lmd = new Date(node.getLastModified());
			
			/*
			 * Normalize since these dates could be off by 
			 * milliseconds...
			 */
			final Date ifModifiedSince, lastModified;
			try {
				ifModifiedSince = normalizer.parse(normalizer.format(date));
				lastModified = normalizer.parse(normalizer.format(lmd));
			} catch (ParseException impossible) {
				TrivialExceptionHandler.impossible(this, impossible);
				return Filter.CONTINUE;
			}
			
			if (lastModified.after(ifModifiedSince)) { 
				stripHeader(request);
				return Filter.CONTINUE;
			}
			else {
				response.setEntity(null);
				response.setStatus(Status.REDIRECTION_NOT_MODIFIED);
				return Filter.STOP;
			}
		}
	}
	
	/**
	 * Remove the if-modified-since header.  This will disallow a Conditional-GET from 
	 * happening.
	 * @param request
	 */
	private void stripHeader(Request request){
		final Form headers = (Form) request.getAttributes().get("org.restlet.http.headers");
		headers.removeAll("If-Modified-Since");
	}

}
