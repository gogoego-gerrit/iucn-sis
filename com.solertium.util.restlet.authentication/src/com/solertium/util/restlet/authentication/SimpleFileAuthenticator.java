/*
 * Copyright (C) 2000-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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
package com.solertium.util.restlet.authentication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.solertium.util.TrivialExceptionHandler;

/**
 * SimpleFileAuthenticator.java
 *
 * @author carl.scott
 *
 */
public class SimpleFileAuthenticator extends FileAuthenticator {

	private final File file;
	
	public SimpleFileAuthenticator(final String location) {
		this(location, false);
	}
	
	public SimpleFileAuthenticator(final String location, final boolean formatOnLoad) {
		super();
		this.file = new File(location);
		if (!file.exists())
			writeFile("");
		if (formatOnLoad)
			doFormat();
	}
	
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new FileReader(file));
	}
	
	public Writer getWriter() throws IOException  {
		return new BufferedWriter(new PrintWriter(new FileWriter(file)));
	}
	
	public String getFileContents() {
		BufferedReader r;
		try {
			r = getReader();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		String line = "";
		String contents = "";
		try {
			while ((line = r.readLine()) != null)
				contents += line;
		} catch (IOException e) {
			contents = null;
		}
		
		try {
			r.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		return contents;
	}
	
}
