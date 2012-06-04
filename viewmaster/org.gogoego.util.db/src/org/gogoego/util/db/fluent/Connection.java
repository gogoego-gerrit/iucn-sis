package org.gogoego.util.db.fluent;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.ExecutionContext;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.SystemExecutionContext;
import org.gogoego.util.db.query.Query;
import org.gogoego.util.getout.GetOut;

public class Connection {
	
	private final ExecutionContext ec;
	
	public Connection(String s){
		this.ec = new SystemExecutionContext(s);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
	}
	
	public void query(String sql, RowProcessor processor) throws DBException {
		ec.doQuery(sql, processor);
	}

	public void query(Query q, RowProcessor processor) throws DBException {
		ec.doQuery(q, processor);
	}

	public void update(String sql) {
		try{
			ec.doUpdate(sql);
		} catch (DBException oops) {
			GetOut.log(oops,sql);
		}
	}
	
	public void updateOrFail(String sql) throws DBException {
		ec.doUpdate(sql);
	}

}
