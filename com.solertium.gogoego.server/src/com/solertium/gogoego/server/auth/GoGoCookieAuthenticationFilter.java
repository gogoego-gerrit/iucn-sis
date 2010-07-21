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
package com.solertium.gogoego.server.auth;

import java.util.Map;

import org.gogoego.api.authentication.CookieAuthenticatorFactory;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.util.restlet.authentication.CookieAuthenticationFilter;
import com.solertium.util.restlet.authentication.CookieAuthenticator;

public class GoGoCookieAuthenticationFilter extends CookieAuthenticationFilter {
	
	public static final String COOKIE_AUTHENTICATED = "gogo:cookie-authenticated";
	
	public GoGoCookieAuthenticationFilter(Context context, String siteID) {
		super(context);
		setDefaultBehavior(Filter.STOP);
		GoGoDebug.get("config", siteID).println("-- Adding cookie authenticators...");
		for (Map.Entry<String, CookieAuthenticatorFactory> entry : PluginAgent.getCookieAuthenticatorBroker().getPlugins().entrySet()) {
			updateAuthenticator(entry.getKey(), entry.getValue());
		}
		GoGoDebug.get("config", siteID).println("-- Done Adding cookie authenticators...");
	}
	
	public void updateAuthenticator(String domain, CookieAuthenticatorFactory factory) {
		final CookieAuthenticator authenticator;
		try {
			authenticator = factory.newInstance(GoGoEgo.get().getFromContext(getContext()));
		} catch (Throwable e) {
			GoGoEgo.debug("error").println("Could not load {0} due to error: {1}", domain, e);
			return;
		}
				
		GoGoEgo.debug("fine").println("Adding cookie authenticator {0}", domain);
		addAuthenticator(domain, authenticator);
	}
	
	/**
	 * We must always continue so that the GoGoGuard can check if 
	 * need be.  However, pass a flag if we are authenticated; 
	 * GoGoGuard will look for it.
	 */
	protected int beforeHandle(Request request, Response response) {
		int result = super.beforeHandle(request, response);
		if (result == Filter.CONTINUE)
			request.getAttributes().put(COOKIE_AUTHENTICATED, Boolean.TRUE);
		return Filter.CONTINUE;
	}
	
	protected void accept(Request request, Response response) {
		request.getAttributes().put(COOKIE_AUTHENTICATED, Boolean.TRUE);
		super.accept(request, response);
	}
	
	protected void doChallenge(CookieAuthenticator authenticator, Request request, Response response) {
		final Reference reference = authenticator.getLoggedOutURL(getFreshCookies(request));
		if (reference.getHostDomain() == null) {
			Request top = request;
			if (top instanceof InternalRequest)
				top = ((InternalRequest)top).getFirstRequest();
			Reference refRef;
			if (top.getReferrerRef() != null)
				refRef = top.getReferrerRef();
			else
				refRef = top.getResourceRef();
			reference.setProtocol(refRef.getSchemeProtocol());
			reference.setHostDomain(refRef.getHostDomain());
			reference.setHostPort(refRef.getHostPort());
		}
		response.redirectTemporary(reference);
	}

}
