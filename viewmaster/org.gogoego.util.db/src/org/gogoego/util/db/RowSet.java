/**
 * 
 */
package org.gogoego.util.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;

public class RowSet implements DBProcessor {
	private final List<Row> set = new ArrayList<Row>();
	
	public static List<Row> get(String query, ExecutionContext ec){
		RowSet rs = new RowSet();
		try{
			ec.doQuery(query, rs);
		} catch (Exception x) {
			GetOut.log(x);
		}
		return rs.getSet();
	}

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
			GetOut.log(ignored);
		}
		final ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next())
			set.add(ec.getDBSession().rsToRow(rs, rsmd));
	}
}