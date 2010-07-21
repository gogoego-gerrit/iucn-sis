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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import org.restlet.util.Series;
import org.w3c.dom.Document;

import com.solertium.util.restlet.RestletUtils;

/**
 * CookieAuthenticationFilter.java
 * 
 * Handles authentication via cookies.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CookieAuthenticationFilter extends Filter {
	
	private final Map<String, CookieAuthenticator> authenticators;
	private int defaultBehavior = Filter.CONTINUE;
	
	public CookieAuthenticationFilter(Context context) {
		super(context);
		authenticators = new HashMap<String, CookieAuthenticator>();
	}
	
	public void addAuthenticator(String domain, CookieAuthenticator authenticator) {
		authenticators.put(domain, authenticator);
	}
	
	public boolean contains(String domain) {
		return authenticators.containsKey(domain);
	}
	
	public void removeAuthenticator(String domain) {
		authenticators.remove(domain);
	}
	
	private Collection<CookieAuthenticator> getAuthenticators(String domain) {
		final Collection<CookieAuthenticator> list;
		if (domain == null || !authenticators.containsKey(domain))
			list = authenticators.values();
		else {
			list = new ArrayList<CookieAuthenticator>();
			list.add(authenticators.get(domain));
		}
		return list;	
	}
	
	protected int beforeHandle(Request request, Response response) {
		if (authenticators.isEmpty())
			return defaultBehavior;
		
		try {
			handleAuth(request, response);
		} catch (ResourceException e) {
			response.setStatus(e.getStatus());
		}
		
		if (response.getStatus().isSuccess())
			return Filter.CONTINUE;
		else
			return Filter.STOP;
	}
	
	public void setDefaultBehavior(int defaultBehavior) {
		this.defaultBehavior = defaultBehavior;
	}
	
	private final void handleAuth(Request request, Response response) throws ResourceException {
		final String domain = RestletUtils.getHeader(request, "authenticationDomain");
		if (request.getResourceRef().getRemainingPart().startsWith("/authn/origin")) {
			if (isValidated(domain, request, response)) {
				response.setStatus(Status.SUCCESS_ACCEPTED);
			}
		}
		else if (request.getResourceRef().getRemainingPart().startsWith("/authn")) {
			if (Method.POST.equals(request.getMethod())) {
				Document document;
				try {
					document = new DomRepresentation(request.getEntity()).getDocument();
				} catch (Exception e) {
					throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, e);
				}
				
				String u = document.getElementsByTagName("u").item(0).getTextContent();
				String oldP = document.getElementsByTagName("oldP").item(0).getTextContent();
				String newP = document.getElementsByTagName("newP").item(0).getTextContent();
				
				CookieAuthenticator authenticator = validate(domain, u, oldP, request, response);
				if (authenticator != null) {
					Cookie cookie = authenticator.changePassword(u, newP);
					if (cookie != null) {
						response.getCookieSettings().removeAll(cookie.getName());
						response.getCookieSettings().add(new CookieSetting(cookie.getName(), cookie.getValue()));
						response.setStatus(Status.SUCCESS_OK);
					}
					else
						response.setStatus(Status.CLIENT_ERROR_LOCKED);
				}
				else
					response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			}
		}
		else if (hasLoginInformation(domain, request) && isValidated(domain, request, response)) {
			accept(request, response);
		}
		else {
			Collection<CookieAuthenticator> list = getAuthenticators(domain);
			if (list.isEmpty())
				response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			else 
				doChallenge(list.iterator().next(), request, response);
		}
	}
	
	protected void accept(Request request, Response response) {
		if (!request.getResourceRef().getPath().startsWith("/authn"))
			getNext().handle(request, response);
		else
			response.setStatus(Status.SUCCESS_OK, "Authentication Accepted.");			
	}
	
	protected void doChallenge(CookieAuthenticator authenticator, Request request, Response response) {
		final Reference reference = authenticator.getLoggedOutURL(getFreshCookies(request));
		if (reference == null) {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
		}
		else {
			if (reference.getHostDomain() == null) {
				reference.setProtocol(request.getResourceRef().getSchemeProtocol());
				reference.setHostDomain(request.getResourceRef().getHostDomain());
				reference.setHostPort(request.getResourceRef().getHostPort());
			}
			response.redirectTemporary(reference);
		}
	}
	
	private boolean hasLoginInformation(String domain, Request request) {
		for (CookieAuthenticator authenticator : getAuthenticators(domain)) 
			if (authenticator.hasLoginInformation(getFreshCookies(request)))
				return true;
		return false;
	}
	
	private CookieAuthenticator validate(String domain, Request request, Response response) {
		return validate(domain, null, null, request, response);
	}
	
	private CookieAuthenticator validate(String domain, String user, String password, Request request, Response response) {
		for (CookieAuthenticator authenticator : getAuthenticators(domain)) {
			Cookie cookie = authenticator.validateAccount(user, password, getFreshCookies(request));
			if (cookie != null) {
				response.getCookieSettings().removeAll(cookie.getName());
				response.getCookieSettings().add(new CookieSetting(cookie.getName(), cookie.getValue()));
				return authenticator;
			}
		}
		return null;
	}

	private boolean isValidated(String domain, Request request, Response response) {
		return validate(domain, request, response) != null;
	}
	
	@SuppressWarnings("unchecked")
	protected final Series<Cookie> getFreshCookies(Request request) {
		return (Series<Cookie>)Series.unmodifiableSeries(request.getCookies());
	}
	
}
