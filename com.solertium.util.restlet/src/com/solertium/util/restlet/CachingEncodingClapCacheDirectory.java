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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.engine.http.HttpConstants;
import org.restlet.resource.Directory;


/**
 * This version of ClapCacheDirectory will encode the payload based on the file extensions
 * supplied, and the Encoding type supplied. This will also cache the encoded representations
 * to avoid the overhead of encoding the payload every time it's requested. This is most
 * useful for sites that serve very large files.
 * 
 * If a file has the Cache-control header of no-cache, this will NOT CACHE the encoded version;
 * it will encode it if necessary.
 * 
 * @author adam.schwartz
 *
 */
public class CachingEncodingClapCacheDirectory extends Directory {

	private static ClassLoader classLoader = null;

	public static void setClassLoader(ClassLoader cl){
		synchronized(ClapCacheDirectory.class){
			if(classLoader==null) classLoader = cl;
		}
	}

	private HashMapRepresentationCache cache;
	private Encoding encodingType;

	/**
	 * Constructor
	 * 
	 * @param context - the Context
	 * @param uri - the absolute root URI
	 * @param extensionsToEncode - file extensions to encode, without '.' as prefix
	 * @param encodingType - Restlet Encoding type
	 */
	public CachingEncodingClapCacheDirectory(Context context,String uri,List<String> extensionsToEncode,Encoding encodingType){
		super(context,uri);
		cache = new HashMapRepresentationCache(extensionsToEncode);
		this.encodingType = encodingType;
	}

	@Override
	public void handle(Request request, Response response){
		ClassLoader saved = null;
		if(classLoader!=null){
			saved = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoader);
		}
		try{
			String p = request.getResourceRef().getPath();
			if("/".equals(p)){
				Reference index = new Reference(request.getResourceRef(),"index.html").getTargetRef();
				response.redirectPermanent(index);
			} else {
				boolean cached = isCached(request, response);
				if( !cached )
					super.handle(request,response);

				if(Status.SUCCESS_OK.equals(response.getStatus())){
					if(p.indexOf("nocache")==-1){
						if(response.getEntity()!=null){
							response.getEntity().setModificationDate(new Date());
							response.getEntity().setExpirationDate(new Date(System.currentTimeMillis()+86400000));
							Form additionalHeaders = new Form();
							additionalHeaders.add("Cache-control", "max-age: 86400, must-revalidate");
							response.getAttributes().put(HttpConstants.ATTRIBUTE_HEADERS,
									additionalHeaders);
						} else {
							Form additionalHeaders = new Form();
							additionalHeaders.add("Cache-control", "no-cache");
							response.getAttributes().put(HttpConstants.ATTRIBUTE_HEADERS,
									additionalHeaders);
						}
					} else {
						Form additionalHeaders = new Form();
						additionalHeaders.add("Cache-control", "no-cache");
						response.getAttributes().put(HttpConstants.ATTRIBUTE_HEADERS,
								additionalHeaders);
					}

					if( !cached )
						encode(request, response);
				}
			}
		} catch (RuntimeException x) {
			throw(x);
		} catch (Error e) {
			throw(e);
		} finally {
			if(saved!=null){
				Thread.currentThread().setContextClassLoader(saved);
			}
		}
	}

	private boolean isCached(Request request, Response response) {
		if( cache.contains(request.getResourceRef().getPath() ) ) {
			response.setEntity(cache.getCachedEntity(request.getResourceRef().getPath()));
			return true;
		} else
			return false;
	}

	private void encode(Request request, Response response) {
		String extension = request.getResourceRef().getLastSegment().substring(
				request.getResourceRef().getLastSegment().lastIndexOf('.')+1 );

		String cacheControl = RestletUtils.getHeader(response, "Cache-control");

		if( !"no-cache".equalsIgnoreCase(cacheControl) ) {
			try {
				response.setEntity( cache.putUnwrappedEntity(extension, encodingType, 
						request.getResourceRef().getPath(), response.getEntity()));
			} catch (IOException e) {
				e.printStackTrace();

				//Encoding fail. Just cache an unencoded version.
				try {
					response.setEntity( cache.putUnwrappedEntity(null, 
							request.getResourceRef().getPath(), response.getEntity()));
				} catch (IOException e1) {
					e1.printStackTrace();
					//Shouldn't be able to get here, since no encoding is performed
				}
			}
		} else if( cache.shouldEncode(extension)) {
			response.setEntity(new EncodeRepresentation(encodingType, response.getEntity()));
		}

		response.setStatus(Status.SUCCESS_OK);
	}

}
