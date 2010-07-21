/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
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

package com.solertium.db.query;

import com.solertium.db.Column;
import com.solertium.db.DBSession;
import com.solertium.db.Row;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class InsertQuery extends BaseQuery implements Query {

	protected Row row = null;

	protected String table = null;

	public InsertQuery() {
	}

	public InsertQuery(final String table, final Row row) {
		setTable(table);
		setRow(row);
	}

	public Row getRow() {
		return row;
	}

	public String getSQL(final DBSession ds) {
		final StringBuffer buf = new StringBuffer(1024);
		buf.append("INSERT INTO ");
		buf.append(ds.formatIdentifier(getTable()));
		buf.append(" (");
		final int cols = row.size();
		for (int i = 0; i < cols; i++) {
			final Column c = row.get(i);
			buf.append(ds.formatIdentifier(c.getLocalName()));
			if (i < cols - 1)
				buf.append(",");
		}
		buf.append(") VALUES (");
		for (int i = 0; i < cols; i++) {
			final Column c = row.get(i);
			buf.append(ds.formatLiteral(c.getLiteral()));
			if (i < cols - 1)
				buf.append(",");
		}
		buf.append(")");
		return buf.toString();
	}

	public String getTable() {
		return table;
	}

	public void setRow(final Row row) {
		this.row = row;
	}

	public void setTable(final String table) {
		this.table = table;
	}

}
