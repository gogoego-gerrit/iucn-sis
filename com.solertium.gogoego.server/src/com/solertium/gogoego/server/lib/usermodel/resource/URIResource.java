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

package com.solertium.gogoego.server.lib.usermodel.resource;

/**
 * References some document that can be goverend by a rule.
 * 
 * @author adam.schwartz
 * 
 */
public class URIResource implements Resource {
	private String uri;

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof String)
			return uri.startsWith((String) obj) || ((String) obj).startsWith(uri);

		return false;
	}

	public String getRepresentation() {
		return getURI();
	}

	public String getURI() {
		return uri;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}
}
