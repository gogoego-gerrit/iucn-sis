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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.MD5Hash;
import com.solertium.util.TrivialExceptionHandler;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class Row {

	public static class Loader implements DBProcessor {
		private Row row = null;

		public Row getRow() {
			return row;
		}

		public void process(final ResultSet rs, final ExecutionContext ec)
				throws Exception {
			try {
				if (!rs.isBeforeFirst())
					return; // no rows, do NOTHING.
			} catch (final SQLException ignored) { // possibly operation not
				// supported
				TrivialExceptionHandler.ignore(this, ignored);
			}
			final ResultSetMetaData rsmd = rs.getMetaData();
			if(rs.next())
				row = ec.getDBSession().rsToRow(rs, rsmd);
		}
	}

	public static class Set implements DBProcessor {
		private final List<Row> set = new ArrayList<Row>();

		public List<Row> getSet() {
			return set;
		}

		public void process(final ResultSet rs, final ExecutionContext ec)
				throws Exception {
			try {
				if (!rs.isBeforeFirst())
					return; // no rows, do NOTHING.
			} catch (final SQLException ignored) { // possibly operation not
				// supported
				TrivialExceptionHandler.ignore(this, ignored);
			}
			final ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next())
				set.add(ec.getDBSession().rsToRow(rs, rsmd));
		}
	}

	public static Row load(final DBSession ds, final String table,
			final Number id) throws Exception {
		final QConstraint constraint = new QComparisonConstraint(
				new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);
		return Row.load(ds, table, constraint);
	}

	public static Row load(final DBSession ds, final String table,
			final QConstraint constraint) throws DBException {
		final ExecutionContext ec = new BackgroundExecutionContext(Row.class
				.getName());
		final SelectQuery sq = new SelectQuery();
		sq.select(new CanonicalColumnName(table, "*"));
		sq.constrain(constraint);
		final Row.Loader rl = new Row.Loader();
		ds.doQuery(sq, rl, ec);
		return rl.getRow();
	}
	
	public Row(){
		super();
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param r Row to copy.
	 */
	public Row(Row r){
		for(Column c : r.getColumns()){
			Column cc = c.getCopy();
			cc.setRow(this);
			this.add(cc);
		}
	}

	public static Row load(final DBSession ds, final String table,
			final String id) throws DBException {
		final QConstraint constraint = new QComparisonConstraint(
				new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);
		return Row.load(ds, table, constraint);
	}

	private final ArrayList<Column> columns = new ArrayList<Column>();

	public void add(final Column c) {
		c.setRow(this);
		columns.add(c);
	}

	public Column get(final int index) {
		return columns.get(index);
	}

	public Column get(final String localName) {
		for (int i = 0; i < columns.size(); i++) {
			final Column c = get(i);
			if (c != null)
				if (localName.equalsIgnoreCase(c.getLocalName()))
					return c;
		}
		return null;
	}
	
	public String getMD5Hash(){
		MD5Hash h = new MD5Hash();
		TreeMap<String,String> tm = new TreeMap<String,String>();
		for(Column c : columns)
			if(!c.isTransient()) tm.put(c.getLocalName(), "");
		for(String cn : tm.keySet())
			get(cn).updateMD5Hash(h);
		return h.toString();
	}

	public ArrayList<Column> getColumns() {
		return columns;
	}

	public void set(final int index, final Column c) {
		c.setRow(this);
		columns.set(index, c);
	}

	public int size() {
		return columns.size();
	}
	
	public Row getStructuredRow(ExecutionContext ec, String tableName) throws DBException {
		Row structured = ec.getRow(tableName);
		for(Column remote : structured.getColumns()){
			Column local = get(remote.getLocalName());
			if(local!=null){
				if(local.getObject()!=null){
					if(remote.getClass().equals(local.getClass())){
						remote.setObject(local.getObject());
					} else {
						try{
							remote.setString(local.toString());
						} catch (ConversionException cx) {
							remote.setObject(null);
						}
					}
				}
			}
		}
		return structured;
	}
	
}
