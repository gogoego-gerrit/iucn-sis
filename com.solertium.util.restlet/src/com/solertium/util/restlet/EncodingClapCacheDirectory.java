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

import java.util.List;

import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.application.EncodeRepresentation;

/**
 * This version of ClapCacheDirectory will encode the payload if it matches one of the file
 * extensions supplied, using the Encoding type supplied.
 * 
 * @author adam.schwartz
 *
 */
public class EncodingClapCacheDirectory extends ClapCacheDirectory {

	private List<String> extensionsToEncode;
	private Encoding encodingType;
	
	public EncodingClapCacheDirectory(Context context,String uri,List<String> extensionsToEncode,Encoding encodingType){
		super(context,uri);
		
		this.extensionsToEncode = extensionsToEncode;
		this.encodingType = encodingType;
	}
	
	@Override
	public void handle(Request request, Response response){
		super.handle(request, response);
		
		if( response.getStatus().isSuccess() ) {
			String extension = request.getResourceRef().getLastSegment().substring(
					request.getResourceRef().getLastSegment().lastIndexOf('.') );
			
			if( extensionsToEncode.contains(extension) )
				response.setEntity(new EncodeRepresentation(encodingType, response.getEntity()));
		}
	}
}
