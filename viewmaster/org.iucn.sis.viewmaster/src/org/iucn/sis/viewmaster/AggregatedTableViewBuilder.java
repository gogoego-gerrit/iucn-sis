package org.iucn.sis.viewmaster;

import java.util.HashMap;
import java.util.Map;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;

public class AggregatedTableViewBuilder {
	
	public void build(final Connection c) throws DBException {
		//table to name/type
		final Map<String, Map<String, String>> map = new HashMap<String, Map<String,String>>();
			
		c.query("select * from universe", new RowProcessor(){
			public void process(Row row) {
				String tableName = row.get("a").getString();
				String name = row.get("c").getString();
				String type = row.get("d").getString();
				
				if("fk_primitive_field".equals(type))
					type = "foreign_key_primitive_field";
				//FIXME this is mishandled but allows view to be created
				if("fk_list_primitive_field".equals(type))
					type = "foreign_key_list_primitive_field";
				//FIXME this is mishandled but allows view to be created
				if("field".equals(type))
					type = "string_primitive_field";
					
				Map<String, String> where = map.get(tableName);
				if (where == null) {
					where = new HashMap<String, String>();
					map.put(tableName, where);
				}
				where.put(name, type);
			}
		});
		
		for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
			GetOut.log("New view: %s", entry.getKey());
			String localViewName = "vw_" + entry.getKey();
						
			c.update(String.format("DROP VIEW IF EXISTS %s CASCADE", localViewName));
			StringBuilder columnspecs = new StringBuilder();
			StringBuilder joinspecs = new StringBuilder();
			StringBuilder subcolumnspecs = new StringBuilder();
			
			int count = 0;
			//Old way...
			/*for (Map.Entry<String, String> columns : entry.getValue().entrySet()) {
				count++;
				if (count == 1)
					joinspecs.append("    JOIN primitive_field pf ON pf.fieldid = field.id\n");
				else {
					columnspecs.append(", ");
					subcolumnspecs.append(", ");
				}
				columnspecs.append("s1."+columns.getKey());
				subcolumnspecs.append("ff"+count+".value as "+columns.getKey());
				joinspecs.append("    LEFT JOIN "+columns.getValue()+" ff"+count+" ON ff"+count+".id = pf.id\n");
			}*/
			//New way
			/*
			 * TODO: implement the above, but based on the existing views.
			 * 
			 * Note that this is fine for normal fields, but may also have some 
			 * odd effects when dealing with classification schemes that we should 
			 * be aware of...
			 */
			/*
			String sql = 
				"CREATE VIEW "+localViewName+" AS SELECT vw_filter.taxonid, vw_filter.assessmentid, "
				+ columnspecs + "\n"
				+ "FROM vw_filter \n"
				+ "LEFT JOIN ( \n"
				+ "  SELECT vw_filter.assessmentid, "
				+ subcolumnspecs + "\n"
				+ "  FROM vw_filter \n"
				+ "  JOIN field on field.assessmentid = vw_filter.assessmentid AND field.name='"+entry.getKey()+"'\n"
				+ joinspecs
				+ ") s1 ON vw_filter.assessmentid = s1.assessmentid";
			c.update(sql);
			c.update("GRANT SELECT ON "+localViewName+" TO iucn");*/
		}
	}

}
