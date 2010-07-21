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
package org.gogoego.api.scripting;

import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Context;

import com.solertium.util.restlet.ContextHelper;

public class ExtendedScriptContextMap {
	
	private ConcurrentHashMap<String,ExtendedScriptContext> map = new ConcurrentHashMap<String,ExtendedScriptContext>();
	
	public static final ExtendedScriptContextMap getInstance(Context context){
		ContextHelper<ExtendedScriptContextMap> emapHelper =
			new ContextHelper<ExtendedScriptContextMap>(ExtendedScriptContextMap.class);
		synchronized(context){
			ExtendedScriptContextMap emap = emapHelper.fetch(context);
			if(emap==null){
				emap = new ExtendedScriptContextMap();
				emapHelper.store(context, emap);
			}
			return emap;
		}
	}
	
	public void put(String key, ExtendedScriptContext esc){
		map.put(key,esc);
	}

	public ExtendedScriptContext get(String key){
		return map.get(key);
	}
	
	public void remove(String key){
		map.remove(key);
	}

}
