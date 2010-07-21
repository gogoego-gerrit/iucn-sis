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
package com.solertium.util.restlet.authorization.base;

import java.util.ArrayList;
import java.util.Collection;

public class BaseAuthorizableObject implements AuthorizableObject {
	
	private final Collection<String> uris;
	private final Collection<String> actions;
	
	public BaseAuthorizableObject() {
		uris = new ArrayList<String>();
		actions = new ArrayList<String>();
	}
	
	public void addAction(String action) {
		if (!actions.contains(action))
			actions.add(action);
	}
	
	public void addUri(String uri) {
		uris.add(uri);
	}
	
	public Collection<String> getAllowedActions() {
		return actions;
	}
	
	public Collection<String> getUris() {
		return uris;
	}

}
