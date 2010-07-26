/**
 * 
 */
package org.gogoego.util.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.gogoego.util.db.query.QComparisonConstraint;
import org.gogoego.util.db.query.QConstraint;
import org.gogoego.util.db.query.SelectQuery;
import org.gogoego.util.db.shared.CanonicalColumnName;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;

public class RowLoader implements DBProcessor {
	private Row row = null;

	public static Row load(final DBSession ds, final String table,
			final Number id) throws Exception {
		final QConstraint constraint = new QComparisonConstraint(
				new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);
		return RowLoader.load(ds, table, constraint);
	}

	public static Row load(final DBSession ds, final String table,
			final QConstraint constraint) throws DBException {
		final ExecutionContext ec = new BackgroundExecutionContext(Row.class
				.getName());
		final SelectQuery sq = new SelectQuery();
		sq.select(new CanonicalColumnName(table, "*"));
		sq.constrain(constraint);
		final RowLoader rl = new RowLoader();
		ds.doQuery(sq, rl, ec);
		return rl.getRow();
	}
	
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
			GetOut.log(ignored);
		}
		final ResultSetMetaData rsmd = rs.getMetaData();
		if(rs.next())
			row = ec.getDBSession().rsToRow(rs, rsmd);
	}
}