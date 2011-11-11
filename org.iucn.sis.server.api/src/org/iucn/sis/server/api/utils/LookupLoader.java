package org.iucn.sis.server.api.utils;

import org.iucn.sis.server.api.application.SIS;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

public class LookupLoader {
	
	public static String get(String fieldName, String primitiveFieldName, int value, boolean includeRef) {
		return get(fieldName, primitiveFieldName, value, includeRef, null);
	}
	
	public static String getByRef(String ref, String primitiveFieldName) {
		return getByRef(ref, primitiveFieldName, null);
	}	

	public static String get(String fieldName, String primitiveFieldName, int value, boolean includeRef, String defaultValue) {
		String tableName = fieldName;
		String columnName1 = "",columnName2 = "";
		if (primitiveFieldName.toLowerCase().endsWith("lookup")) {
			tableName = primitiveFieldName;
			columnName1 = "REF";
			columnName2 = "DESCRIPTION";
		}
		else {
			if (tableName.endsWith("Subfield"))
				tableName = tableName.substring(0, tableName.lastIndexOf("Subfield"));
			tableName += "_" + primitiveFieldName + "Lookup";
			columnName1 = "LABEL";
		}
		
		SelectQuery query = new SelectQuery();
		
		query.select(tableName, columnName1);
		if (!"".equals(columnName2))
			query.select(tableName, columnName2);
		
		query.constrain(new CanonicalColumnName(tableName, "ID"), QConstraint.CT_EQUALS, value);
		
		Row.Loader rl = new Row.Loader();
		
		try {
			SIS.get().getLookupDatabase().doQuery(query, rl);
		} catch (DBException e) {
			return defaultValue;
		}
		
		if (rl.getRow() == null)
			return defaultValue;
		
		if (!"".equals(columnName2))
			return includeRef ? rl.getRow().get(0).toString() + " " + rl.getRow().get(1).toString() :
				rl.getRow().get(1).toString();
		else
			return rl.getRow().get(0).toString();

	}
	
	public static String getByRef(String ref, String primitiveFieldName, String defaultValue) {
		String tableName = "";
		String columnName1 = "";
		
		tableName = primitiveFieldName;
		columnName1 = "DESCRIPTION";
	
		SelectQuery query = new SelectQuery();		
		query.select(tableName, columnName1);		
		query.constrain(new CanonicalColumnName(tableName, "REF"), QConstraint.CT_EQUALS, ref);
		
		Row.Loader rl = new Row.Loader();
		
		try {
			SIS.get().getLookupDatabase().doQuery(query, rl);
		} catch (DBException e) {
			return defaultValue;
		}
		
		if (rl.getRow() == null)
			return defaultValue;
		
		return  ref + " " + rl.getRow().get(0).toString();


	}	
}
