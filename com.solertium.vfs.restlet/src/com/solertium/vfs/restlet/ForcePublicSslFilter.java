/*
 * Copyright (C) 2009 Solertium Corporation
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

package com.solertium.vfs.restlet;

import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.routing.Filter;

/**
 * This Restlet Filter will redirect all HTTP requests for resources
 * from the public Internet to use SSL.  This can work as a good practice
 * reminder for users.
 * <p>
 * *** IMPORTANT ***
 * <p>
 * It is not an enforcement means for two reasons:
 * first, it relies on client IP address detection, which can be wrong
 * or spoofed, and second, because if secrets (like HTTP Basic Auth)
 * are transmitted in the clear to the server BEFORE this Filter
 * redirects, the secrets are still exposed in the clear.
 * <p>
 * If you're not using the standard ports (80 and 443) this Filter assumes
 * your SSL port number is one higher than your HTTPS port number.
 * <p>
 * You may also set an explicit https port if desired. 
 * 
 * @author rob.heittman@solertium.com
 * @author carl.scott@solertium.com
 *
 */
public class ForcePublicSslFilter extends Filter {
	
	private int httpsPort = -1;
	
	public ForcePublicSslFilter(Context context){
		super(context);
	}
	
	public ForcePublicSslFilter(Context context, Class<? extends Resource> resource) {
		super(context);
		setNext(resource);
	}
	
	public void setHTTPSPort(int httpsPort) {
		this.httpsPort = httpsPort;
	}
	
	public int beforeHandle(Request request, Response response){
		if(request.isConfidential()) return super.beforeHandle(request, response); // already SSL
		if (isPrivateNetwork(request)) {
			return super.beforeHandle(request, response); // private network
		} else {
			// external network, force redirect
			format(request, response);
			return Filter.STOP;
		}
	}
	
	protected void format(Request request, Response response) {
		final Reference r = new Reference(request.getResourceRef());
		r.setScheme(Protocol.HTTPS.getSchemeName());
		if (r.getHostPort() > 0)
			r.setHostPort(r.getHostPort()==80?-1:httpsPort==-1?r.getHostPort()+1:httpsPort);
		response.redirectPermanent(r);
	}
	
	protected boolean isPrivateNetwork(Request request) {
		String clientAddress = request.getClientInfo().getAddress();
		if(clientAddress == null) clientAddress = "";
		return (clientAddress.startsWith("10.") ||
			clientAddress.startsWith("172.16.") ||
			clientAddress.startsWith("192.168.") ||
			clientAddress.startsWith("127.") ||
			request.getProtocol().equals(Protocol.RIAP));
	}
	
}
