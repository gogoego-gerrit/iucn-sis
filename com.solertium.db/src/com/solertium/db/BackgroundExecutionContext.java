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

import java.io.StringWriter;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.naming.NamingException;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class BackgroundExecutionContext extends ExecutionContext {
	final boolean d;
	final Logger log;
	StringWriter w = new StringWriter(2048);

	public BackgroundExecutionContext(final String logkey) {
		log = Logger.getLogger(logkey);
		d = log.isLoggable(Level.FINE);
	}

	public BackgroundExecutionContext(final String logkey,
			final DBSession dbsession) {
		this(logkey);
		setDBSession(dbsession);
	}

	public BackgroundExecutionContext(final String logkey,
			final String databaseName) throws NamingException {
		log = Logger.getLogger(logkey);
		d = log.isLoggable(Level.FINE);
		setDBSession(DBSessionFactory.getDBSession(databaseName));
	}

	@Override
	public void br() {
		write("\n");
	}

	@Override
	public void debug(final String s) {
		if (d) {
			if (s == null)
				return;
			log.fine(s);
		}
	}

	@Override
	public void endblock(final String blockid) {
		write("\n");
	}

	@Override
	public void error(final String s) {
		if (s == null)
			return;
		log.severe(s);
	}

	@Override
	public void error(final String s, final Throwable t) {
		if (s == null)
			return;
		log.log(Level.SEVERE, s, t);
	}

	@Override
	public void flush() {
		String s = w.toString();
		if (s.endsWith("\n"))
			s = s.substring(0, s.length() - 1);
		log.info(s);
		w = new StringWriter(2048);
	}

	@Override
	public Object getContextObject() {
		return log;
	}

	@Override
	public void startblock(final String blockid) {
	}

	@Override
	public void warn(final String s) {
		if (s == null)
			return;
		log.warning(s);
	}

	@Override
	public void write(final String s) {
		w.write(s);
		if (s.endsWith("\n"))
			flush();
	}

	@Override
	public void writeln(final String s) {
		super.writeln(s);
	}
}
