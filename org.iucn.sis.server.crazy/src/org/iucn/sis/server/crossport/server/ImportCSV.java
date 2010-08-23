package org.iucn.sis.server.crossport.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.CSVTokenizer;
import com.solertium.util.SysDebugger;

public class ImportCSV {

	public static void main(String[] args) throws Exception {

		DBSessionFactory.registerDataSource("target", "jdbc:access:////usr/data/rldbRelationshipFree.mdb",
				"com.hxtt.sql.access.AccessDriver", "", "");

		SysDebugger.getInstance().println("Connecting to target database.");
		ExecutionContext ectarget = new SystemExecutionContext("target");
		ectarget.setExecutionLevel(ExecutionContext.ADMIN);
		ectarget.setAPILevel(ExecutionContext.SQL_ALLOWED);

		try {
			ectarget.doUpdate("drop table REDLIST_PUB");
		} catch (Exception failOk) {
			SysDebugger.getInstance().println("REDLIST_PUB doesn't already exist");
		}
		ectarget
				.doUpdate("create table REDLIST_PUB (ASSESSMENTID integer,REDLISTDATASOURCEID integer,OTHERDATASOURCEID integer)");

		BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(
				"/home/rob.heittman/Desktop/RedList_Pub.txt"), "UTF-8"));

		while (fr.ready()) {
			String line = fr.readLine();
			if (line == null)
				break;
			try {
				CSVTokenizer tok = new CSVTokenizer(line);
				String assessmentId = tok.nextToken();
				String redListDataSourceId = tok.nextToken();
				if ("".equals(redListDataSourceId)) {
					redListDataSourceId = "NULL";
				}
				tok.nextToken(); // string version discarded
				String otherListDataSourceId = tok.nextToken();
				if ("".equals(otherListDataSourceId)) {
					otherListDataSourceId = "NULL";
				}
				String s = "insert into REDLIST_PUB" + " values (" + assessmentId + "," + redListDataSourceId + ","
						+ otherListDataSourceId + ");";
				SysDebugger.getInstance().println(s);
				ectarget.doUpdate(s);
				SysDebugger.getInstance().println("Imported citation info for " + assessmentId);
			} catch (Exception oops) {
				oops.printStackTrace();
			}
		}

	}

}
