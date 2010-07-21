package org.iucn.sis.server.etl;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.iucn.sis.server.ref.ReferenceApplication;
import org.w3c.dom.Document;

import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.TableCopier;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.SysDebugger;

/**
 * Copy references out of DEM data files
 * 
 * @author rob.heittman
 */
public class CopyReferences {

	public static void main(final String[] args) throws Exception {

		// link to the reference data here
		DBSessionFactory.registerDataSource("userdata", "jdbc:access:///ETP2.mdb", "com.hxtt.sql.access.AccessDriver",
				"sa", "");

		// link to the DEM application here
		DBSessionFactory.registerDataSource("dem", "jdbc:access:///sisDataEntryModule.mdb",
				"com.hxtt.sql.access.AccessDriver", "sa", "");

		// link to the target database here
		/*
		 * DBSessionFactory.registerDataSource("target",
		 * "jdbc:mysql://devlnx03/bib", "com.mysql.jdbc.Driver", "tomcat",
		 * "s3cr3t");
		 */

		DBSessionFactory.registerDataSource("target", "jdbc:h2:bin/h2_db/refdb", "org.h2.Driver", "sa", "");

		Document structDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				ReferenceApplication.class.getResourceAsStream("refstruct.xml"));

		SysDebugger.getInstance().println("Connecting to target database.");
		final ExecutionContext ectarget = new SystemExecutionContext("target");
		ectarget.setExecutionLevel(ExecutionContext.ADMIN);

		List<String> tables = ectarget.getDBSession().listTables(ectarget);
		SysDebugger.getInstance().println("Applying new structure");
		for (String tn : tables) {
			SysDebugger.getInstance().println("Drop table:" + tn);
			ectarget.dropTable(tn);
		}
		ectarget.createStructure(structDoc);

		new SystemExecutionContext("userdata").setStructure(structDoc);

		TableCopier.copy("dem", "reference_types", "target", "reference_types");
		TableCopier.copy("userdata", "bibliographic_original_records", "target", "bibliography_temp");
		TableCopier.copy("userdata", "Systematics", "target", "systematics");
		TableCopier.copy("userdata", "bibliography_link", "target", "bibliography_link");

		SelectQuery sq = new SelectQuery();
		sq.select("bibliography_temp", "*");
		ectarget.doQuery(sq, new RowProcessor() {

			@Override
			public void process(Row ri) {
				try {
					Row row = ri.getStructuredRow(getExecutionContext(), "bibliography");
					SysDebugger.getInstance().println("MD5 Set: " + row.getMD5Hash());
					Column hash = row.get("Bib_hash");
					hash.setObject(row.getMD5Hash());
					ectarget.doUpdate(new InsertQuery("bibliography", row));
				} catch (DBException dbx) {
					dbx.printStackTrace();
				}
			}

		});

		ectarget.dropTable("bibliography_temp");

		SysDebugger.getInstance().println("Done.");

	}

}
