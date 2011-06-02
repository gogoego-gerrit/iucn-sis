package org.iucn.sis.viewmaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;

public class TaxonomyViewBuilder {
	
	public void build(Connection c) throws DBException, IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("taxonomyviews.sql")	
		));
		
		StringBuilder read = new StringBuilder();
		String line = null;
		
		while ((line = reader.readLine()) != null) {
			read.append(line + "\n");
			if (line.endsWith(";")) {
				String sql = read.toString();
				GetOut.log(sql);
				c.update(sql);
				read = new StringBuilder();
			}
		}
	}

}
