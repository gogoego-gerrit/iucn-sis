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
package org.gogoego.api.applications;

import org.gogoego.api.representations.GoGoEgoBaseRepresentation;

/**
 * TemplateDataAPI.java
 * 
 * Public interface for retrieving data associated with registered templates.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface TemplateDataAPI {
	
	/**
	 * Get the content type
	 * @return
	 */
	public String getContentType();
	
	/**
	 * Get the display name
	 * @return
	 */
	public String getDisplayName();
	
	/**
	 * Gets a representation of this template
	 */
	public GoGoEgoBaseRepresentation getRepresentation();
	
	/**
	 * Get the template's uri
	 * @return
	 */
	public String getUri();
	
	public boolean equals(Object obj);

	/**
	 * Returns true if this template is allowed to template the 
	 * item at the given uri, false otherwise.
	 * @param uri
	 * @return
	 */
	public boolean isAllowed(String uri);
	
	public String toString();

}
