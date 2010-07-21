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
package org.gogoego.api.errors;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

public interface ErrorHandler {
	
	/**
	 * Handles a request that we know has already 404'd.  
	 * The response should then contain the status of the 
	 * request after the handler has attempted to handle 
	 * the error, as well as the entity to be returned to 
	 * the client.
	 * 
	 * If this handler can not process this request, it 
	 * can fail-fast by throwing a ResourceException or 
	 * returning a null response.
	 *   
	 * @param context the context
	 * @param request the request that 404'd
	 * @return the response, or null if the failed request
	 *  can not be handled by this error handler
	 */
	public Response handle404(Context context, Request request) throws ResourceException;

}
