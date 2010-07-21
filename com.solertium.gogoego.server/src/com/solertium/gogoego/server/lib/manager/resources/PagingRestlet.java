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
package com.solertium.gogoego.server.lib.manager.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.BoundedHashMap;
import com.solertium.util.restlet.CookieUtility;

/**
 * PagingRestlet.java
 * 
 * Provides simple paging for the very long debugging output, 
 * 100 lines at a time.  Pages are cached as a list and identified 
 * by a single key each time a request is made.  Up to 50 sets of 
 * pages can be stored at one time.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class PagingRestlet extends Restlet {
	
	//How many pages to keep in memory
	private static final int MAX_PAGES = 100;
	
	//How may lines to display;
	private static final int PAGE_LENGTH = 100;
	
	private final BoundedHashMap<String, List<String>> cachedPages; 
	private final String extension;	
	private final String vmroot;

	public PagingRestlet(Context context, String vmroot, String extension) {
		super(context);
		this.extension = extension;
		this.vmroot = vmroot;
		this.cachedPages = new BoundedHashMap<String, List<String>>(50);
	}

	public void handle(Request request, Response response) {
		String siteID = (String)request.getAttributes().get("siteID");
		String cacheKey = (String)request.getAttributes().get("key");
		String pageStr = (String)request.getAttributes().get("num");
		
		try {
			response.setEntity(represent(request, response, siteID, cacheKey, pageStr));
			response.setStatus(Status.SUCCESS_OK);
		} catch (ResourceException e) {
			e.printStackTrace();
			response.setStatus(e.getStatus());
		}
	}
	
	public Representation represent(Request request, Response response, String siteID, String cacheKey, String pageStr) throws ResourceException {
		if (siteID == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		if (cacheKey == null) 
			return createPages(response, siteID);
		else if (pageStr == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a page number.");
		else 
			return getPage(cacheKey, pageStr);
	}
	
	private Representation createPages(Response response, String siteID) throws ResourceException {
		final List<String> pages = new LinkedList<String>();
		
		final BufferedReader reader;
		try {
			int version = GoGoDebug.getVersion(vmroot, siteID, extension.substring(1), false);
			reader = new BufferedReader(new FileReader(new File(
				vmroot + File.separator + "_logs" + File.separator + siteID + "[" + version + "]" + extension
			)));
		} catch (IOException e) {
			response.setEntity(new StringRepresentation(
				"Console unavailable, please try again later.", MediaType.TEXT_PLAIN
			));
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		StringBuilder builder = new StringBuilder();
		String line;
	
		try {
			int count = 0;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append('\n');
				if (++count > PAGE_LENGTH) {
					if (pages.size() > MAX_PAGES)
						pages.remove(0);
					pages.add(builder.toString());
					count = 0;
					builder = new StringBuilder();
				}
			}
			if (count > 0) {
				if (pages.size() > MAX_PAGES)
					pages.remove(0);
				pages.add(builder.toString());
			}
		} catch (IOException e) {
			response.setEntity(new StringRepresentation(
				"Console unavailable, please try again later.", MediaType.TEXT_PLAIN
			));
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final String id = CookieUtility.newUniqueID();
		
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("root");
		root.appendChild(BaseDocumentUtils.impl.createElementWithText(document, "key", id));
		root.appendChild(BaseDocumentUtils.impl.createElementWithText(document, "size", Integer.toString(pages.size())));
		document.appendChild(root);
		
		cachedPages.put(id, pages);
		
		return new GoGoEgoDomRepresentation(document);
	}
	
	private Representation getPage(String cacheKey, String pageStr) throws ResourceException {
		final List<String> pages;
		if ((pages = cachedPages.get(cacheKey)) == null)
			throw new ResourceException(Status.CLIENT_ERROR_GONE);
		
		int pageNumber;
		try {
			pageNumber = Integer.parseInt(pageStr);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply an integer.");
		}
		
		if (pageNumber < 0 || pageNumber >= pages.size())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Page number out of bounds.  Please supply a number between 0 and " + pages.size());
		
		return new StringRepresentation(pages.get(pageNumber), MediaType.TEXT_PLAIN);
	}
	
}
