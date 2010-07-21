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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.lib.scripting.GoGoScriptEngineManager;

public class ScriptEngineBroker extends PluginBroker<ScriptEngineFactory> {
	
	public static final GoGoScriptEngineManager manager = new GoGoScriptEngineManager();
	
	public static void list() {
		List<ScriptEngineFactory> factories = manager.getEngineFactories();
		if (factories.isEmpty())
			GoGoDebug.system().println("WARNING: There are no script engine factories installed.");
		else {
			GoGoDebug.system().println("Script engine factories installed:");
			for (ScriptEngineFactory factory : factories)
				GoGoDebug.system().println(" + {0}", factory.getEngineName());
		}
	}
	
	private final Map<String, Collection<String>> engines;
	
	public ScriptEngineBroker(BundleContext context) {
		super(context, ScriptEngineFactory.class.getName());
		
		engines = new HashMap<String, Collection<String>>();
		
		addListener(new PluginListener<ScriptEngineFactory>() {
			public void onModified(ScriptEngineFactory service, ServiceReference reference) {
				onRegistered(service, reference);
			}
			public void onRegistered(ScriptEngineFactory service, ServiceReference reference) {
				GoGoDebug.system().println("Registering Script Service {0}", service.getClass().getName());
				final String registrationScheme = manager.getRegistrationScheme(service);
				manager.register(service);
				Collection<String> list = engines.get(registrationScheme);
				if (list == null)
					list = new ArrayList<String>();
				list.add(registrationScheme);
				engines.put(reference.getBundle().getSymbolicName(), list);
				list();
			}
			public void onUnregistered(String symbolicName) {
				release(symbolicName);
				final Collection<String> registrationSchemes = engines.remove(symbolicName);
				if (registrationSchemes != null) {
					for (String registrationScheme : registrationSchemes) {
						final ScriptEngineFactory service = manager.unregister(registrationScheme);
						if (service != null)
							GoGoDebug.system().println("Un-Registered Script Service {0}", service.getClass().getName());
					}
				}
				list();
			}
			
		});
	}
	
	public ScriptEngine getEngineByName(String shortName) {
		return manager.getEngineByName(shortName);
	}
	
	public ScriptEngine getEngineByExtension(String extension) {
		return manager.getEngineByExtension(extension);
	}

}
