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
import java.util.List;

/**
 * Optional functions for more advanced script running than provided for by
 * JSR223 directly.  Wrap a JSR223 factory implementation and implement this
 * interface to provide portable access to these features.
 */
public interface ExtendedScriptEngineFactory {

	/**
	 * Return a context that can be used to run scripts supported by the
	 * attached libraries.
	 * 
	 * @param libraryPaths
	 * @return
	 */
	public ExtendedScriptContext getExtendedScriptContext(List<URL> libraryPaths);
	
}
