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

package com.solertium.gogoego.server.lib.usermodel.access;

import java.util.Date;

import com.solertium.gogoego.server.lib.usermodel.PolicedAccount;
import com.solertium.gogoego.server.lib.usermodel.resource.Resource;
import com.solertium.gogoego.server.lib.usermodel.resource.ResourceAccessType;

/**
 * A rule represents a mapping of a User to a Resource
 * 
 * @author adam.schwartz
 * 
 */
public class Rule implements Resolveable {
	private final ResourceAccessType accessRule;
	private final Date expiry;
	private final Resource resource;
	private final PolicedAccount signer;

	public Rule(final PolicedAccount user, final Resource resource, final ResourceAccessType accessRule,
			final Date expires) {
		signer = user;
		this.resource = resource;
		this.accessRule = accessRule;
		expiry = new Date(expires.getTime());
	}

	public ResourceAccessType getAccessRule() {
		return accessRule;
	}

	public Resource getResource() {
		return resource;
	}

	public PolicedAccount getSigner() {
		return signer;
	}

	public boolean isExpired() {
		return expiry.after(new Date());
	}

	public boolean resolve(final PolicedAccount user, final Resource resource) {
		if (expiry.after(new Date()))
			return false;

		if (user.equals(signer) && resource.equals(this.resource))
			return true;

		return false;
	}

	public String toXML() {
		String xml = "<rule>\r\n";
		xml += "<resource value=\"" + resource.getRepresentation() + "\" />\r\n";
		if (accessRule.canRead())
			xml += "<read />";
		if (accessRule.canWrite())
			xml += "<write />";
		xml += "</rule>\r\n";

		return xml;
	}
}
