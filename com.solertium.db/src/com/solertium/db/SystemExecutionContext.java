/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */

package com.solertium.db;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.naming.NamingException;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class SystemExecutionContext extends ExecutionContext {
	final PrintWriter out;

	public SystemExecutionContext() {
		out = new PrintWriter(new OutputStreamWriter(System.out));
	}

	public SystemExecutionContext(final DBSession dbsession) {
		this();
		setDBSession(dbsession);
	}

	public SystemExecutionContext(final String databaseName)
			throws NamingException {
		this();
		setDBSession(DBSessionFactory.getDBSession(databaseName));
	}

	@Override
	public void br() {
		out.write("\n");
	}

	@Override
	public synchronized void debug(final String s) {
		out.write(s);
		out.write("\n");
		out.flush();
	}

	@Override
	public void endblock(final String blockid) {
		out.write("\n");
	}

	@Override
	public void flush() {
		out.flush();
	}

	@Override
	public Object getContextObject() {
		return null;
	}

	@Override
	public void startblock(final String blockid) {
	}

	@Override
	public void write(final String s) {
		if (s == null) {
			out.write("null");
			return;
		}
		out.write(s);
		flush();
	}

	@Override
	public synchronized void writeln(final String s) {
		super.writeln(s);
	}
}
