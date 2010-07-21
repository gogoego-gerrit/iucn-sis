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

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBSession;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class DeleteQuery extends BaseQuery implements Query {

	protected String table = null;

	public DeleteQuery() {
	}

	public DeleteQuery(final String table) {
		setTable(table);
	}

	public DeleteQuery(final String table, final QConstraint constraint) {
		setTable(table);
		constrain(constraint);
	}

	public DeleteQuery(final String table, final String field,
			final Object equals) {
		setTable(table);
		constrain(new QComparisonConstraint(new CanonicalColumnName(table,
				field), QConstraint.CT_EQUALS, equals));
	}

	public String getSQL(final DBSession ds) {
		final StringBuffer buf = new StringBuffer(1024);
		buf.append("DELETE FROM ");
		buf.append(ds.formatIdentifier(getTable()));
		if (!constraints.isEmpty()) {
			buf.append(" WHERE ");
			buf.append(constraints.getSQL(ds));
		}
		return buf.toString();
	}

	public String getTable() {
		return table;
	}

	public void setTable(final String table) {
		this.table = table;
	}

}
