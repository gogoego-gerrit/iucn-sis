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
package com.solertium.gogoego.server.lib.settings.cache;

import java.io.IOException;

import com.solertium.gogoego.server.lib.settings.SimpleSettingsReader;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFSPath;

public class CacheSettingsLoader extends SimpleSettingsReader {
	
	public static final VFSPath SETTINGS_PATH = new VFSPath("/(SYSTEM)/settings/cache.xml"); 
	
	public CacheSettingsLoader() {
		super();
		try {
			load(SETTINGS_PATH);
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

}
