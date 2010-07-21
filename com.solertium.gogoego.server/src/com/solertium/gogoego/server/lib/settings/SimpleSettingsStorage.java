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
package com.solertium.gogoego.server.lib.settings;

import java.util.HashMap;
import java.util.Map;

import com.solertium.gogoego.server.lib.settings.cache.CacheSettingsLoader;

public class SimpleSettingsStorage {
	
	private final Map<String, SimpleSettingsReader> map;
	
	public SimpleSettingsStorage() {
		map = new HashMap<String, SimpleSettingsReader>();
	}
	
	public SimpleSettingsReader get(String key) {
		if (map.containsKey(key))
			return map.get(key);
		else {
			final SimpleSettingsReader loader = createLoader(key);
			if (loader != null)
				map.put(key, loader);
			return loader;
		}
	}
	
	public void flush(String key) {
		map.remove(key);
	}
	
	/**
	 * TODO: should this be a broker?
	 */
	private SimpleSettingsReader createLoader(String key) {
		if ("cache".equals(key))
			return new CacheSettingsLoader();
		//TODO: the rest
		else
			return null;
	}

}
