package org.iucn.sis.viewmaster;

import java.io.IOException;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;

public class VWFilterViewBuilder {
	
	public void build(Connection c, final String schema, final String user) throws DBException, IOException {
		final String file;
		if ("vw_published".equals(schema))
			file = "RedListPublish3.sql"; 
		else if ("vw_drafts".equals(schema))
			file = "AllDrafts.sql";
		else if ("vw_all".equals(schema))
			file = "AllTaxa.sql";
		else
			return;
		
		for (String line : new SQLReader(file)) {
			String sql = line;
			sql = sql.replace("$schema", schema);
			GetOut.log(sql);
			c.update(sql);
		}
	}

}
