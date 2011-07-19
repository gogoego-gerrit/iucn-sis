package org.iucn.sis.viewmaster;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;

public class AdditionalViewBuilder {
	
	private final List<String> files;
	
	public AdditionalViewBuilder() {
		files = new ArrayList<String>();
	}
	
	public void addFile(String file) {
		files.add(file);
	}

	public void build(final Connection c, final String schema, final String user) throws DBException, IOException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -10);
		
		String caveat = format.format(cal.getTime());
		for (String file : files) {
			for (String line : new SQLReader(file)) {
				String sql = line;
				sql = sql.replace("$schema", schema);
				sql = sql.replace("$user", user);
				sql = sql.replace("$caveat", caveat);
				GetOut.log(sql);
				c.update(sql);
			}
		}
	}
	
}
