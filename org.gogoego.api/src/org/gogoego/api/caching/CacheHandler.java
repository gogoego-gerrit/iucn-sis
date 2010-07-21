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
package org.gogoego.api.caching;

import org.restlet.Context;

/**
 * CacheHandler.java
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface CacheHandler {
	
	/**
	 * Clears the memory cache completely.
	 */
	public void clearCache();
	
	/**
	 * Remove this URI and anything associated with it 
	 * from the cache.
	 * @param uri
	 */
	public void removeFromCache(Context context, String uri);

}
