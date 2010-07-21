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
package com.solertium.gogoego.server.auth;

import java.util.Map;

import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.authentication.AuthenticatorFactory;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.restlet.authentication.Authenticator;
import com.solertium.util.restlet.authentication.MultiDomainAuthnGuard;

/**
 * GoGoGuard.java
 * 
 * Guard implementation
 * 
 * @author carl.scott
 * 
 */
public class GoGoGuard extends MultiDomainAuthnGuard {
	
	private final ServerApplicationAPI api;

	public GoGoGuard(Context context, ChallengeScheme scheme, String siteID) {
		super(context, scheme, siteID);
		this.api = GoGoEgo.get().getFromContext(context);
		GoGoDebug.get("config", getRealm()).println("-- Adding basic authenticators...");
		for (Map.Entry<String, AuthenticatorFactory> entry : PluginAgent.getAuthenticatorBroker().getPlugins().entrySet()) {
			updateAuthenticator(entry.getKey(), entry.getValue());
		}
		GoGoDebug.get("config", getRealm()).println("-- Done Adding basic authenticators...");
	}
	
	/**
	 * Used to reload authenticators following a drop in or initialization.
	 * @param domain
	 * @param factory
	 */
	public void updateAuthenticator(String domain, AuthenticatorFactory factory) {
		final Authenticator authenticator;
		try {
			authenticator = factory.newInstance(api);
		} catch (Throwable e) { //Throwable due to OSGi
			GoGoEgo.debug("error").println("Could not load {0} due to error: {1}", domain, e);
			return;
		}
		
		GoGoEgo.debug("config").println("Adding authenticator {0} to {1}", domain, getRealm());
		addAuthenticator(domain, authenticator);
	}
	
	public void removeAuthenticator(String domain) {
		authenticators.remove(domain);
	}

	protected boolean bypassAuth(Request request) {
		return request.getAttributes().containsKey(GoGoCookieAuthenticationFilter.COOKIE_AUTHENTICATED) || 
			ServerApplication.getFromContext(getContext()).isHostedMode();
	}

	protected void addNewProfile(String username, String password, Response response, Document doc) {
		response.setEntity(username, MediaType.TEXT_PLAIN);
	}

	protected void setDefaultAuthenticator() {
		// Nothing to do
	}

}
