package org.iucn.sis.server.api.utils;

import org.iucn.sis.server.api.application.SIS;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

public class LookupLoader {
	
	public static String get(String fieldName, String primitiveFieldName, int value) {
		return get(fieldName, primitiveFieldName, value, null);
	}
	
	public static String get(String fieldName, String primitiveFieldName, int value, String defaultValue) {
		String tableName = fieldName;
		String columnName;
		if (primitiveFieldName.toLowerCase().endsWith("lookup")) {
			//TODO: want to also pull "REF" and join the two strings together
			tableName = primitiveFieldName;
			columnName = "DESCRIPTION";
		}
		else {
			if (tableName.endsWith("Subfield"))
				tableName = tableName.substring(0, tableName.lastIndexOf("Subfield"));
			//FIXME: this one's obvious...
			if ("UseTrade".equals(tableName))
				tableName += "Details_" + primitiveFieldName + "Lookup";
			else
				tableName += "_" + primitiveFieldName + "Lookup";
			columnName = "LABEL";
		}

		SelectQuery query = new SelectQuery();
		query.select(tableName, columnName);
		query.constrain(new CanonicalColumnName(tableName, "ID"), QConstraint.CT_EQUALS, value);
		
		Row.Loader rl = new Row.Loader();
		
		try {
			SIS.get().getLookupDatabase().doQuery(query, rl);
		} catch (DBException e) {
			return defaultValue;
		}
		
		return rl.getRow() == null ? defaultValue : rl.getRow().get(0).toString();
	}

}
