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

import java.util.Date;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.http.HttpConstants;
import org.restlet.resource.Directory;

public class ClapCacheDirectory extends Directory {

	private static ClassLoader classLoader = null;
	
	public static void setClassLoader(ClassLoader cl){
		synchronized(ClapCacheDirectory.class){
			if(classLoader==null) classLoader = cl;
		}
	}
	
	public ClapCacheDirectory(Context context,String uri){
		super(context,uri);
	}
	
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
					}
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

}
