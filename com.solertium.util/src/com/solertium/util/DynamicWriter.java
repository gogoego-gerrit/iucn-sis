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
package com.solertium.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.solertium.util.TrivialExceptionHandler;

/**
 * DynamicWriter.java
 * 
 * Use this to write to places other than System.out seamlessly.
 * 
 * @author carl.scott
 * 
 */
public class DynamicWriter {

	private Writer out;
	private String lineBreakRule;

	public DynamicWriter() {
		setOutputStream(new BufferedWriter(new PrintWriter(System.out)), "\r\n");
	}
	
	public void close() {
		try {
			out.close();
		} catch (IOException ignored) {
			TrivialExceptionHandler.ignore(this, ignored);
		}
	}

	public void setOutputStream(Writer writer, String lineBreakRule) {
		this.out = writer;
		this.lineBreakRule = lineBreakRule;
	}

	protected void write(String line) {
		try {
			out.write(line + lineBreakRule);
			out.flush();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

}
