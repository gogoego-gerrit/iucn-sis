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

import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.usermodel.PolicedAccount;
import com.solertium.gogoego.server.lib.usermodel.access.Rule;

/**
 * Base class for a resource accessor.
 * 
 * @author adam.schwartz
 * 
 */
public abstract class ResourceAccessor {
	/**
	 * Should actually perform the fetching of the resource, returning it as a
	 * Document object.
	 * 
	 * @param resource
	 *            to be fetched
	 * @return fetched Document
	 */
	protected abstract Document fetchResource(Resource resource);

	/**
	 * Performs a check to ensure the provided User and Resource match the given
	 * Rule, and that the access rules allow for reading.
	 * 
	 * @param resource
	 *            to be fetched
	 * @param resourceRule
	 *            rule associated with the resource and user
	 * @param user
	 *            performing the operation
	 * @return
	 */
	public Document getResource(final Resource resource, final Rule resourceRule, final PolicedAccount user) {
		if (resourceRule.resolve(user, resource) && resourceRule.getAccessRule().canRead())
			return fetchResource(resource);

		return null;
	}

	/**
	 * Should actually perform the storing of the resource, returning whether or
	 * not it succeeded.
	 * 
	 * @param resource
	 *            to be stored
	 * @return success or not
	 */
	protected abstract boolean putResource(Resource resource, Object obj);

	/**
	 * Performs a check to ensure the provided User and Resource match the given
	 * Rule, and that the access rules allow for reading.
	 * 
	 * @param resource
	 *            to be updated
	 * @param resourceRule
	 *            - rule associated with the resource and user
	 * @param user
	 *            performing the operation
	 * @return whether the "put" happened or not
	 */
	public boolean storeDocumentToResource(final Resource resource, final Rule resourceRule, final PolicedAccount user,
			final Object obj) {
		if (resourceRule.resolve(user, resource) && resourceRule.getAccessRule().canWrite())
			return putResource(resource, obj);

		return false;
	}
}
