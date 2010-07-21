package org.iucn.sis.server.api.utils;

import java.sql.ResultSet;

import com.solertium.db.DBProcessor;
import com.solertium.db.ExecutionContext;

public class SelectCountDBProcessor implements DBProcessor {
	public int count;

	public int getCount() {
		return count;
	}

	public void process(ResultSet rs, ExecutionContext ec) throws Exception {
		rs.next();
		count = rs.getInt(1);
	}
}
