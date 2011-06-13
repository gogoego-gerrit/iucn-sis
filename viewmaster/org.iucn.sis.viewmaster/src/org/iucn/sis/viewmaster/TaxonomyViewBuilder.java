package org.iucn.sis.viewmaster;

import java.io.IOException;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;

public class TaxonomyViewBuilder {
	
	private String currentSchema = "public";
	
	public void setCurrentSchema(String currentSchema) {
		if (currentSchema != null)
			this.currentSchema = currentSchema;
	}
	
	public void build(Connection c, final String schema, final String user) throws DBException, IOException {
		for (String line : new SQLReader("taxonomyviews.sql")) {
			String sql = line;
			sql = sql.replace(" vw_", " " + schema + ".vw_");
			sql = sql.replace("taxon.", "public.taxon.");
			sql = sql.replace(" taxon ", " public.taxon ");
			sql = sql.replace("TO iucn", "TO " + user);
			GetOut.log(sql);
			c.update(sql);
		}
	}
	
	public String tbl(String name) {
		return currentSchema + "." + name;
	}
	
	public void update(Connection c, String query, Object... params) {
		c.update(String.format(query, params));
	}

}
