package org.iucn.sis.viewmaster;

import java.io.IOException;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;

public class VWFilterViewBuilder {
	
	public void build(Connection c, final String schema, final String user) throws DBException, IOException {
		for (String line : new SQLReader("RedListPublish_2.sql")) {
			String sql = line;
			sql = sql.replace("$schema", schema);
			GetOut.log(sql);
			c.update(sql);
		}
	}

}
