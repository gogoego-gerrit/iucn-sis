package org.iucn.sis.server.crossport.demimport;

import java.util.List;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.TableCopier;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.SysDebugger;

public class OverlayNewTables {

	public static void main(String[] args) throws Exception {

		SysDebugger.getInstance().println("Setting up source databases.");
		DBSessionFactory.registerDataSource("source", "jdbc:access:////home/rob.heittman/Desktop/sisnew/newTables.mdb",
				"com.hxtt.sql.access.AccessDriver", "", "");

		SysDebugger.getInstance().println("Setting up target database.");
		DBSessionFactory.registerDataSource("target",
				"jdbc:access:////home/rob.heittman/Desktop/sisnew/rldbRelationshipFree.mdb",
				"com.hxtt.sql.access.AccessDriver", "", "");

		SysDebugger.getInstance().println("Connecting to source database.");
		ExecutionContext ecsource = new SystemExecutionContext("source");

		List<String> tables = ecsource.getDBSession().listTables(ecsource);
		SysDebugger.getInstance().println("Connecting to target database.");
		ExecutionContext ectarget = new SystemExecutionContext("target");
		ectarget.setExecutionLevel(ExecutionContext.ADMIN);

		SysDebugger.getInstance().println("Deleting from existing tables");
		for (String table : tables) {
			SysDebugger.getInstance().println("Delete from table:" + table);
			DeleteQuery dq = new DeleteQuery(table);
			ectarget.doUpdate(dq);
		}

		for (String table : tables) {
			SysDebugger.getInstance().println("Copy table:" + table);
			SelectQuery sq = new SelectQuery();
			sq.select(new CanonicalColumnName(table, "*"));
			ecsource.doQuery(sq, new TableCopier(ectarget, table));
		}

	}

}
