/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;

public class CurrentBinary {

	public static void main(final String[] args) {
		String path = ".";
		try{
			final CurrentBinary cb = new CurrentBinary();
			path = CurrentBinary.getDirectory(cb).toString();
		} catch (RuntimeException rx) {
			path = ".";
		} finally {
			System.out.println(path);
		}
	}
	
	public static File getDirectory(Object cb) {
		final Class<?> qc = cb.getClass();
		final CodeSource source = qc.getProtectionDomain().getCodeSource();
		final URL location = source.getLocation();
		try{
			File f = new File(new URL(location.toString().replaceAll(" ", "%20")).toURI());
			if(f.isFile()) return f.getParentFile();
			return f;
		} catch (URISyntaxException us) {
			throw new RuntimeException("The base URI for the application could not be resolved");
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed URL Exception!");
		}
	}

}
