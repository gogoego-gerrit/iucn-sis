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
package com.solertium.gogoego.server.cm;

import org.gogoego.api.scripting.ScriptableObjectFactory;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleContext;
import org.restlet.data.Request;

import com.solertium.gogoego.server.lib.app.tags.scripting.TagBrowseUtility;
import com.solertium.gogoego.server.lib.app.tags.scripting.TagSearchResultController;

/**
 * ScriptableObjectBroker.java
 * 
 * Broker scriptable objects.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ScriptableObjectBroker extends PluginBroker<ScriptableObjectFactory> {

	ScriptableObjectBroker(BundleContext bundleContext) {
		super(bundleContext, ScriptableObjectFactory.class.getName());
		
		addHeaderKey("Bundle-Name");
		
		for (String key : new String[] {"TagBrowseUtility", "com.solertium.gogoego.server.lib.app.tags.scripting.TagBrowseUtility"})
			addLocalReference(key, new ScriptableObjectFactory() {
				public Object getScriptableObject(Request request) {
					return new TagBrowseUtility().getScriptableObject(request);
				}
			});
		for (String key : new String[] {"TagSearchResultController", "com.solertium.gogoego.server.lib.app.tags.scripting.TagSearchResultController"})
			addLocalReference(key, new ScriptableObjectFactory() {
				public Object getScriptableObject(Request request) {
					return new TagSearchResultController().getScriptableObject(request);					
				}
			});
	}

	public ScriptableObjectFactory getScriptableObjectFactory(String key, String minimumVersion) {
		return getPlugin(key, minimumVersion);
	}

}
