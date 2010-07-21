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

import org.restlet.data.Cookie;
import org.restlet.data.Reference;
import org.restlet.util.Series;

import com.solertium.util.restlet.authentication.CookieAuthenticator;

public class GoGoCookieAuthenticator extends CookieAuthenticator {
	
	public GoGoCookieAuthenticator() {
		super("gogo:cookie");
	}
	
	public Cookie changePassword(String user, String newPassword) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Reference getLoggedOutURL(Series<Cookie> cookies) {
		return new Reference("/index.html");
	}
	
	public boolean hasLoginInformation(Series<Cookie> cookies) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Cookie validateAccount(String user, String password, Series<Cookie> cookies) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
