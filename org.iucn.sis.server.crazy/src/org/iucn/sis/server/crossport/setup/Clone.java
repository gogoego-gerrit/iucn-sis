package org.iucn.sis.server.crossport.setup;

import java.util.Iterator;
import java.util.List;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBSession;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.TableCopier;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.SysDebugger;

public class Clone {

	public static void main(String[] args) throws Exception {

		SysDebugger.getInstance().println("Setting up source database.");
		DBSession.register_datasource("source", "jdbc:access:////usr/data/2007 Red List_decoded.mdb",
		// "jdbc:access:////home/adam.schwartz/dev/sis/2007 Red List for Jim.zip",
				"com.hxtt.sql.access.AccessDriver", "", "");

		SysDebugger.getInstance().println("Setting up target database.");
		DBSession.register_datasource("target", "jdbc:mysql://localhost/rldbfinal", "com.mysql.jdbc.Driver", "tomcat",
				"s3cr3t");

		ExecutionContext ecsource = new SystemExecutionContext();
		ecsource.setExecutionLevel(ExecutionContext.ADMIN);
		ExecutionContext ectarget = new SystemExecutionContext();
		ectarget.setExecutionLevel(ExecutionContext.ADMIN);

		// java.sql.DriverManager.setLogStream(java.lang.System.out);
		SysDebugger.getInstance().println("Connecting to source database.");
		ecsource.setDBSession(DBSession.get("source"));

		SysDebugger.getInstance().println("Connecting to target database.");
		ectarget.setDBSession(DBSession.get("target"));

		SysDebugger.getInstance().println("Listing tables in source database.");
		List<String> tables = ecsource.getDBSession().listTables(ecsource);

		SysDebugger.getInstance().println("Starting export.");
		Iterator<String> it = tables.iterator();
		while (it.hasNext()) {
			String tn = it.next();
			SysDebugger.getInstance().println("Export table:" + tn);
			SelectQuery sq = new SelectQuery();
			sq.select(new CanonicalColumnName(tn, "*"));
			ecsource.doQuery(sq, new TableCopier(ectarget, tn));
		}
	}

}
