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
package com.solertium.gogoego.server.lib.manager.container;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;
import org.restlet.security.Guard;

/**
 * ManagerGuard.java
 * 
 * A simple guard that protects the manager application separately from the rest
 * of the GoGoEgo sites.
 * 
 * @author carl.scott
 * 
 */
public class ManagerGuard extends Guard {

	private final String username;
	private final String password;

	public ManagerGuard(Context context, ChallengeScheme scheme, String realm, final String username,
			final String password) {
		super(context, scheme, realm);
		this.username = username;
		this.password = password;
	}

	public int doHandle(Request request, Response response) {
		if (request.getProtocol().equals(Protocol.RIAP)) {
			return Filter.CONTINUE;
		} else if (System.getProperty("HOSTED_MODE", "false").equals("true")){
			return Filter.CONTINUE;		
		} else
			return super.doHandle(request, response);
	}

	public int authenticate(final Request request) {
		return super.authenticate(request);
	}

	public boolean checkSecret(Request request, final String identifier, final char[] secret) {
		try {
			return username.equals(identifier) && password.equals(new String(secret));
		} catch (NullPointerException e) {
			return false;
		}
	}
}
