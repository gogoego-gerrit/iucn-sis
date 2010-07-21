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

import java.net.URL;

import javax.script.ScriptException;

/**
 * This interface identifies valid long-running script contexts such as Rails or Django.
 * These contexts are tightly bound to one particular ExtendedScriptEngineFactory, and contain a
 * valid pointer to it (preferably held in a weak reference or equivalent).  Probably
 * this represents a language runtime or pool of runtimes sharing some kind of state.
 */
public interface ExtendedScriptContext {
	
	/**
	 * @return the engine factory that this ExtendedScriptContext is associated with
	 */
	public ExtendedScriptEngineFactory getEngineFactory();

	/**
	 * Runs a script.  Caller must supply the actual script source as a String
	 * as well as the URL of the script so that relative references can be resolved.
	 * 
	 * @param script
	 * @param source
	 * @param context
	 */
	public Object executeScript(URL source) throws ScriptException;
	
	/**
	 * Evaluates a fragment of script.  Caller must supply a symbolicName which
	 * can be used to identify the script (but not necessarily resolve relative
	 * references, since none may exist).
	 * 
	 * @param script
	 * @param symbolicName
	 * @param context
	 */
	public Object eval(String script, String symbolicName) throws ScriptException;

}
