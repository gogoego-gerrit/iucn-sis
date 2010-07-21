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
import java.util.Collections;
import java.util.Iterator;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;

import com.solertium.util.SysDebugger;

/**
 * MultiDomainAuthnGuard.java
 * 
 * Leverages the domain set of AuthnGuard to check against 
 * ALL domains if one is not specified.
 *
 * @author carl.scott
 *
 */
public abstract class MultiDomainAuthnGuard extends AuthnGuard {
	
	private static final SysDebugger log = SysDebugger.getNamedInstance("debug");
	
	public MultiDomainAuthnGuard(Context context, ChallengeScheme scheme, String realm) {
		super(context, scheme, realm);
	}
	
	/*
	 * By default (with no domain specified), all known 
	 * Authenticators will be validated against.
	 */
	protected boolean validate(String id, char[] secret) {
		boolean isValidated = false;
		final String secretStr = new String(secret);
		final ArrayList<Authenticator> list = 
			new ArrayList<Authenticator>(authenticators.values());
		
		Collections.sort(list);		
		Iterator<Authenticator> iterator = list.listIterator();
		
		while (iterator.hasNext() && !(isValidated = iterator.next().
			hasCachedSuccess(id, secretStr)));
		
		if (!isValidated) {
			log.println("Found no cached success, attempting to validate");
			iterator = list.listIterator();
			while (iterator.hasNext() && !(isValidated = iterator.next().
				validateAccount(id, secretStr)));
		}
		
		return isValidated;
	}
	
	/*
	 * The standing implementation ignores if there was no 
	 * domain explicitly set and uses it.  I will instead 
	 * check all known domains unless one was explicitly set.
	 */
	protected boolean validate(String id, char[] secret, String domain) {
		return (domain != null) ? super.validate(id, secret, domain) : validate(id, secret);
	}	

}
