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
package com.solertium.gogoego.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TraversalLog {
	public static final TraversalLog instance = new TraversalLog();

	private final Writer fw;

	private TraversalLog() {
		File f = new File("/Users/robheittman/traversal.log");
		Writer w;
		try {
			FileOutputStream fos = new FileOutputStream(f, true);
			w = new OutputStreamWriter(fos);
		} catch (FileNotFoundException fnf) {
			System.out.println("Could not create traversal log");
			w = null;
		}
		fw = w;
	}

	public static void log(String s) {
		if (instance.fw == null)
			return;
		try {
			instance.fw.write(s);
			instance.fw.write("\n");
			instance.fw.flush();
		} catch (IOException nowrite) {
			System.out.println("Could not write to traversal log");
		}
	}
}
