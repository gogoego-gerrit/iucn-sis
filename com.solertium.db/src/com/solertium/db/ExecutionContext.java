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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import net.jcip.annotations.NotThreadSafe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.query.Query;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
@NotThreadSafe
public abstract class ExecutionContext {
	public final static int ADMIN = 4;
	public final static int API_ONLY = 1;
	public final static int READ_ONLY = 1;

	public final static int READ_WRITE = 2;
	public final static int SQL_ALLOWED = 2;

	private int apiLevel = API_ONLY;
	private int executionLevel = READ_ONLY;

	DBSession sess;

	public Document analyzeExistingStructure() throws DBException {
		return getDBSession().analyzeExistingStructure(this);
	}

	public void appendStructure(final Document structureDoc, final boolean create) throws DBException {
		getDBSession().appendStructure(structureDoc, this, create);
	}

	public abstract void br();

	public void createStructure(final Document structureDoc) throws DBException {
		getDBSession().createStructure(structureDoc, this);
	}

	public void createTable(final Element e) throws DBException {
		getDBSession().createTable(e, this);
	}

	public void createTable(final String table, final Row prototype) throws DBException {
		getDBSession().createTable(table, prototype, this);
	}

	public abstract void debug(String s);

	public void doQuery(final Query q, final DBProcessor processor) throws DBException {
		getDBSession().doQuery(q, processor, this);
	}

	public void doQuery(final String sql, final DBProcessor processor) throws DBException {
		getDBSession().doQuery(sql, processor, this);
	}

	public void doUpdate(final Query q) throws DBException {
		getDBSession().doUpdate(q, this);
	}

	public void doUpdate(final String sql) throws DBException {
		getDBSession().doUpdate(sql, this);
	}

	public void dropTable(final String table) throws DBException {
		getDBSession().dropTable(table, this);
	}

	public abstract void endblock(String blockid);

	public void error(final String s) {
		writeln("ERROR: " + s);
	}

	public synchronized void error(final String s, final Throwable x) {
		if (x == null)
			error(s);
		final String xclass = x.getClass().getName();
		final String xmsg = x.getMessage();
		if (xmsg == null)
			writeln("ERROR: " + s + " (" + xclass + ")");
		else
			writeln("ERROR: " + s + " (" + xclass + ": " + xmsg + ")");
	}

	public void flush() {
	}

	public String formatLiteral(final Literal l) {
		return sess.formatLiteral(l);
	}

	public int getAPILevel() {
		return apiLevel;
	}

	public abstract Object getContextObject();

	public DBSession getDBSession() {
		return sess;
	}

	public int getExecutionLevel() {
		return executionLevel;
	}

	/**
	 * Retrieves a list of tables that match the given table name. You should
	 * call this method and get a table to use for a getRow() call to be ensured
	 * that the table you supply is a valid table
	 * 
	 * @param tableName
	 *            the table name to match
	 * @return a list of tables
	 */
	public ArrayList<String> getMatchingTables(final String tableName) throws DBException {
		final Iterator<String> it = getDBSession().listTables(this).listIterator();
		final ArrayList<String> list = new ArrayList<String>();
		while (it.hasNext()) {
			final String cur = it.next();
			if (cur.equalsIgnoreCase(tableName))
				list.add(cur);
		}
		return list;
	}

	/**
	 * Gets a template row for a given table
	 * 
	 * @see getMatchingTables()
	 * 
	 * @param tableName
	 *            the table
	 * @return the template row
	 * @throws DBException
	 * 
	 */
	public Row getRow(final String tableName) throws DBException {
		return getDBSession().getRow(tableName, this);
	}

	public void setAPILevel(final int apiLevel) {
		this.apiLevel = apiLevel;
	}

	public void setDBSession(final DBSession sess) {
		this.sess = sess;
	}

	public void setExecutionLevel(final int executionLevel) {
		this.executionLevel = executionLevel;
	}

	public void setStructure(final Document structureDoc) throws DBException {
		getDBSession().setStructure(structureDoc, this);
	}

	public void startblock() {
		final String id = UUID.randomUUID().toString();
		startblock(id);
	}

	public abstract void startblock(String blockid);

	public void warn(final String s) {
		writeln("WARNING: " + s);
	}

	public abstract void write(String s);

	public void writeln(final String s) {
		write(s);
		br();
	}
}
