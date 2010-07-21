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
package com.solertium.util.restlet.authentication;

import org.restlet.data.Cookie;
import org.restlet.data.Reference;
import org.restlet.util.Series;

/**
 * CookieAuthenticator.java
 * 
 * Implement the simple authenticator for your application's 
 * specific authentication needs.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * @author liz.schwartz
 *
 */
public abstract class CookieAuthenticator {
	
	protected final String cookieName;
	
	public CookieAuthenticator(String cookieName) {
		this.cookieName = cookieName;
	}
	
	protected final Cookie getCookie(Series<Cookie> cookies) {
		return cookies.getFirst(cookieName);
	}
	
	protected final String getCookieValue(Series<Cookie> cookies) {
		final Cookie cookie = getCookie(cookies);
		return cookie == null ? null : cookie.getValue();
	}

	/**
	 * Redirect to a URL to tell the user they have been 
	 * logged out
	 * @param cookies an immutable set of cookies from the request
	 * @return the redirect reference, or null if no redirect should be performed.
	 */
	public abstract Reference getLoggedOutURL(Series<Cookie> cookies);
	
	/**
	 * Validate the account, return a cookie if valid
	 * @param user
	 * @param password
	 * @param cookies an immutable set of cookies from the request
	 * @return the cookie, or null if invalid
	 */
	public abstract Cookie validateAccount(String user, String password, Series<Cookie> cookies);
	
	
	public abstract Cookie changePassword(String user, String newPassword);
	
	/**
	 * Determine if login information is available
	 * @param cookies an immutable set of cookies from the request
	 * @return true if login information is available, false otherwise
	 */
	public abstract boolean hasLoginInformation(Series<Cookie> cookies);

}
