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

import org.restlet.Context;

/**
 * IsPrivate.java
 * 
 * If your plugin is intended to be private, that is, it's only 
 * available for use on specific site instances, implement this 
 * interface.  Users will not be able to install this application 
 * unless the implementing class specifies that it can via the 
 * isSiteAllowed function.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 *
 */
public interface IsPrivate {
	
	/**
	 * Determine if the given site is allowed to install 
	 * this application.  Called when someone attempts to 
	 * load or install this application, and if it is 
	 * determined that the application can not be installed 
	 * for a given site, it will not be displayed as an 
	 * option in the standard GoGoEgo Studio interface. 
	 * @param context the context
	 * @param siteID the site id
	 * @return true if this site is allowed this application, false otherwise
	 */
	public boolean isSiteAllowed(Context context, String siteID);

}
