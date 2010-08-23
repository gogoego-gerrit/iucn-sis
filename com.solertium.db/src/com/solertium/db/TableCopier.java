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

import javax.naming.NamingException;

import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.SelectQuery;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class TableCopier extends RowProcessor {

	public static void copy(final String sourceDatabaseName,
			final String sourceTableName, final String destinationDatabaseName,
			final String destinationTableName) throws NamingException,
			DBException {
		final ExecutionContext ecsrc = new SystemExecutionContext(
				sourceDatabaseName);
		ecsrc.setExecutionLevel(ExecutionContext.ADMIN);
		final ExecutionContext ecdest = new SystemExecutionContext(
				destinationDatabaseName);
		ecdest.setExecutionLevel(ExecutionContext.ADMIN);
		final SelectQuery sq = new SelectQuery();
		sq.select(new CanonicalColumnName(sourceTableName, "*"));
		ecsrc.doQuery(sq, new TableCopier(ecdest, destinationTableName));
	}

	private int count = 0;
	private final DBSession dbtarget;
	private final ExecutionContext ectarget;
	private final String tablename;

	public TableCopier(final ExecutionContext ectarget, final String tablename) {
		this.ectarget = ectarget;
		dbtarget = ectarget.getDBSession();
		this.tablename = tablename;
	}

	@Override
	public void process(final Row sourceRow) {
		final InsertQuery q = new InsertQuery();
		try {
			Row targetRow = ectarget.getRow(tablename);
			for(Column c : targetRow.getColumns()){
				Column t = sourceRow.get(c.getLocalName());
				if(t!=null) c.setObject(t.getObject());
			}
			q.setTable(tablename);
			q.setRow(targetRow);
			dbtarget.doUpdate(q, ectarget);
		} catch (final Exception recorded) {
			getExecutionContext().writeln(q.getSQL(dbtarget));
			getExecutionContext().writeln(
					"  Exception: " + recorded.getClass().getName() + ": "
							+ recorded.getMessage());
		}
		count++;
		if (count % 1000 == 0) {
			getExecutionContext().writeln("  " + count + "...");
			getExecutionContext().flush();
		}
	}

}
