package org.iucn.sis.viewmaster;

import java.io.IOException;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;

public class AdditionalViewBuilder {

	public void build(final Connection c, final String schema, final String user) throws DBException, IOException {
		for (String line : new SQLReader("additional.sql")) {
			String sql = line;
			sql = sql.replace("$schema", schema);
			sql = sql.replace("$user", user);
			GetOut.log(sql);
			c.update(sql);
		}
	}
	
}
