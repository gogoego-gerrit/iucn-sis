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
package org.gogoego.api.representations;

import org.restlet.data.MediaType;

/**
 * GoGoEgoRepresentation.java
 * 
 * Simple representation information.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface GoGoEgoRepresentation {
	
	/**
	 * Get the content.  The content should always be available 
	 * for fetching.  
	 * @return
	 */
	public String getContent();
	
	/**
	 * Get the content type for this resource, which is different 
	 * from the media type as this can be collection/item or 
	 * store/cart, in addition to text/html.
	 * @return
	 */
	public String getContentType();
	
	/**
	 * Get the preferred media type for this representation. 
	 * This may be deprecated in the future.
	 * @return
	 */
	public MediaType getPreferredMediaType();
	
}
