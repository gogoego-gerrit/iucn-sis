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
 *     http://www.eclipse.org/legal/epl-v10.html
 * 
 *  2) The GNU General Public License, version 2 or later
 *     http://www.gnu.org/licenses
 ******************************************************************************/
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
package org.gogoego.api.classloader;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.representations.GoGoEgoInputRepresentation;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.http.HttpConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.util.restlet.MediaTypeManager;

/**
 * SimpleClasspathResource.java
 * 
 * A helper resource that you can integrate with your 
 * applications.  It will take your classloader, presumably 
 * fetched via something using a ClassLoaderActivator, and 
 * return resources from it, without interfering with the 
 * current thread's classloader.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class SimpleClasspathResource extends Resource {
	
	private final String remaining;
	
	public SimpleClasspathResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		String encodedUri = request.getResourceRef().getRemainingPart();
		if ("".equals(encodedUri) || "/".equals(encodedUri))
			encodedUri = "/index.html";
		
		int qindex = encodedUri.indexOf("?");
		if (qindex != -1)
			encodedUri = encodedUri.substring(0, qindex);
		try {
			encodedUri = URLDecoder.decode(encodedUri, "UTF-8");
		} catch (UnsupportedEncodingException ux) {
			throw new RuntimeException("Expected UTF-8 encoding not found in Java runtime");
		}
		
		this.remaining = encodedUri;
		
		getVariants().add(new Variant(MediaTypeManager.getMediaType(remaining)));
	}
	
	/**
	 * Retrieve the base uri for the classloader.  This should be 
	 * everything that will come before what you expect the client's 
	 * remaining part to be for a given request, as the base uri will 
	 * be directly prepended to the remaining part of the request's 
	 * resource reference object.  It should not include a trailing 
	 * slash.
	 * 
	 * @return the base uri
	 */
	public abstract String getBaseUri();
	
	/**
	 * Retrieves the class loader that you want to load you static 
	 * files from.  A typically use case in the context of GoGoEgo 
	 * would be to load your class via a ClassLoaderActivator, then 
	 * GoGoEgo will use a broker to locate this, ensuring that if 
	 * you re-export after starting the application, the freshest 
	 * classloader will be used.
	 * @return the class loader
	 */
	public abstract ClassLoader getClassLoader();
	
	public Representation represent(Variant variant) throws ResourceException {
		ClassLoader cl = getClassLoader();
		if(cl==null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		final InputStream stream = cl.getResourceAsStream(getBaseUri() + remaining);
		if (stream == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		else {
			getResponse().setStatus(Status.SUCCESS_OK);
			
			Representation entity = new GoGoEgoInputRepresentation(stream, variant.getMediaType());
			entity.setModificationDate(new Date());
			entity.setExpirationDate(new Date(System.currentTimeMillis()+86400000));
				
			final Form additionalHeaders = new Form();
			additionalHeaders.add("Cache-control", "max-age: 86400, must-revalidate");
				
			getResponse().getAttributes().put(
				HttpConstants.ATTRIBUTE_HEADERS, additionalHeaders
			);
			
			return entity;
		}
	}
	
	public void addGZIPHeader() {
		getRequest().getAttributes().put(Constants.ALLOW_GZIP, Boolean.TRUE);
	}

}
