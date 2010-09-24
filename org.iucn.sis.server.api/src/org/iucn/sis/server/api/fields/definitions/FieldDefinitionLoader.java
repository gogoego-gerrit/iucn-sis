package org.iucn.sis.server.api.fields.definitions;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.iucn.sis.server.api.application.SIS;
import org.w3c.dom.Document;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.BaseDocumentUtils;

public class FieldDefinitionLoader {
	
	private static List<String> allFields = null;
	
	public static List<String> getAllFields() {
		if (allFields != null)
			return allFields;
		
		allFields = new ArrayList<String>();
		
		ExecutionContext ec = SIS.get().getLookupDatabase();
		
		List<String> tables;
		try  {
			tables = ec.getDBSession().listTables(ec);
		} catch (DBException e) {
			e.printStackTrace();
			return allFields;
		}
		
		for (String table : tables) {
			String tableLower = table.toLowerCase();
			if (table.contains("_") || tableLower.endsWith("lookup") || tableLower.endsWith("subfield"))
				continue;
			
			allFields.add(table);
		}
		
		return allFields;
	}
	
	public static Document get(String fieldName) {
		String file = fieldName;
		if (!fieldName.endsWith(".xml"))
			file += ".xml";
		return BaseDocumentUtils.impl.getInputStreamFile(
			FieldDefinitionLoader.class.getResourceAsStream(file)
		);
	}

}
