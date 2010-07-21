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

import java.util.Collection;

/**
 * TemplateRegistryAPI.java
 * 
 * Public interface for interacting with the template registry.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface TemplateRegistryAPI {
	
	/**
	 * Returns true if a template is registered to the given key 
	 * (file name or path), false otherwise
	 * 
	 * @param key
	 * @return
	 */
	public boolean isRegistered(final String key);
	
	/**
	 * Returns a listing of all template registered in the system.
	 * @return
	 */
	public Collection<TemplateDataAPI> getTemplateListing();
	
	/**
	 * Get data associated with the template registered for 
	 * the given key (file name or path).
	 * @param key
	 * @return
	 */
	public TemplateDataAPI getRegisteredTemplate(String key);

}
