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

package com.solertium.gogoego.server;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;
import org.restlet.security.Guard;

/**
 * AdminAuthnGuard does simple password authentication for an admin account.
 * 
 * @author Rob Heittman <rob.heittman@solertium.com>
 * 
 */
public class AdminAuthnGuard extends Guard {

	public final String adminPassword;

	public AdminAuthnGuard(final Context context, final ChallengeScheme scheme, final String realm,
			final String adminPassword) {
		super(context, scheme, realm);
		if (adminPassword == null)
			this.adminPassword = "changeme"; // default
		else
			this.adminPassword = adminPassword;
	}

	@Override
	public int authenticate(final Request request) {
		if (((ServerApplication) Application.getCurrent()).isHostedMode())
			return 1;

		return super.authenticate(request);
	}

	@Override
	public boolean checkSecret(final Request request, final String identifier, final char[] secret) {

		if (((ServerApplication) Application.getCurrent()).isHostedMode())
			return true;

		final String s = new String(secret);
		if (adminPassword.equals(s))
			return true;
		return false;
	}

	public int doHandle(Request request, Response response) {
		if (request.getResourceRef().getPath().startsWith("/admin/js/forkeditor")
				|| request.getResourceRef().getPath().startsWith("/admin/connectors/gogogadget")) {
			accept(request, response);
			return Filter.CONTINUE;
		}

		return super.doHandle(request, response);
	}

}
