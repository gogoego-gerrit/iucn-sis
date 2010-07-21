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
package org.gogoego.api.filters;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * FileWritingFilter.java
 * 
 * Filter is called before a file is written.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface FileWritingFilter {
	
	/**
	 * Perform the filtering.  If the entity is a text-based representation, you 
	 * will be able to read the entity.  If it is NOT text-based, you can not, and 
	 * an UnsupportedOperationException will be thrown.
	 * 
	 * @param context the context
	 * @param reference the request uri
	 * @param entity the entity, readable if text-based, private otherwise
	 * 
	 * @return the filter results
	 */
	public FilterResults filter(Context context, Reference reference, Representation entity);
	
	/**
	 * True if the results of filtering are processed 
	 * automatically, false if, on failure, the user 
	 * is allowed to make corrections manually.  This 
	 * only matters if the filter fails validation and 
	 * you are able to provide an updated text 
	 * representation.
	 * 
	 * @return true if automatic, false otherwise
	 */
	public boolean isAutomatic();

}
