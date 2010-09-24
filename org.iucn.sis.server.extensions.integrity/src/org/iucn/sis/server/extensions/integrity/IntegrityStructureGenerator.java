package org.iucn.sis.server.extensions.integrity;

import java.util.List;

import javax.naming.NamingException;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;

public class IntegrityStructureGenerator {
	
	public static Document generate() {
		final Document document = BaseDocumentUtils.impl.newDocument();
		document.appendChild(document.createElement("tables"));
		
		ExecutionContext ec = SIS.get().getLookupDatabase();
		/*try {
			ec = new SystemExecutionContext("sis_lookups");
		} catch (NamingException e) {
			e.printStackTrace();
			return document;
		}
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);*/
		
		final List<String> allFields =
			SIS.get().getFieldIO().getAllFields();
		
		for (String field : allFields) {
			final Row.Set rs = new Row.Set(); {
				final SelectQuery query = new SelectQuery();
				query.select(field, "*");
				
				try {
					ec.doQuery(query, rs);
				} catch (DBException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			final Element tableEl = document.createElement("table");
			tableEl.setAttribute("name", field);
			
			for (Row row : rs.getSet()) {
				final String dataType = row.get("data_type").toString();
				final String columnName = row.get("name").toString();
				
				final Element columnEl = document.createElement("column");
				columnEl.setAttribute("name", columnName);
				columnEl.setAttribute("type", getType(dataType));
				
				if (dataType.startsWith("fk")) {
					String lookupTable = field + "_" + columnName + "Lookup";
					try {
						if (!ec.getMatchingTables(lookupTable).isEmpty()) {
							columnEl.setAttribute("relatedTable", lookupTable);
							columnEl.setAttribute("relatedColumn", "id");
						}
					} catch (DBException e) {
						e.printStackTrace();
						TrivialExceptionHandler.ignore(ec, e);
					}
				}
				
				tableEl.appendChild(columnEl);
			}
			
			document.getDocumentElement().appendChild(tableEl);
		}
		
		return document;
	}

	private static String getType(String type) {
		PrimitiveFieldType pType = PrimitiveFieldType.get(type);
		if (pType == null)
			return "CString";
		
		switch (pType) {
			case FOREIGN_KEY_LIST_PRIMITIVE:
			case FOREIGN_KEY_PRIMITIVE:
			case INTEGER_PRIMITIVE:
				return "CInteger";
			case BOOLEAN_PRIMITIVE:
				return "CBoolean";
			case FLOAT_PRIMITIVE:
				return "CLong";
			case DATE_PRIMITIVE:
				return "CDate";
			default:
				return "CString";
		}
	}
}
