package org.iucn.sis.viewmaster;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;

public class SingleColumnViewBuilder {
	
	public void build(final Connection c) throws DBException {
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
				
				GetOut.log("New view: %s.%s", tableName, name);
				String localViewName = "vw_" + tableName + "_" + name;
					
				c.update(String.format("DROP VIEW IF EXISTS %s CASCADE", localViewName));
				StringBuilder columnspecs = new StringBuilder();
				StringBuilder joinspecs = new StringBuilder();
				
				columnspecs.append("ff.value");
				joinspecs.append("    JOIN primitive_field pf ON pf.fieldid = field.id AND field.name = '" + name + "'\n");
				if ("foreign_key_list_primitive_field".equals(type)) {
					joinspecs.append("    JOIN "+type+" fi ON fi.id = pf.id\n");
					joinspecs.append("    JOIN fk_list_primitive_values ff ON ff.fk_list_primitive_id = pf.id\n");
				}
				else
					joinspecs.append("    JOIN "+type+" ff ON ff.id = pf.id\n");
					
					
				String sql = 
					"CREATE VIEW "+localViewName+" AS SELECT vw_filter.taxonid, vw_filter.assessmentid, "
					+ columnspecs + "\n"
					+ "FROM vw_filter \n"
					+ "  JOIN field on field.assessmentid = vw_filter.assessmentid AND field.name='"+tableName+"'\n"
					+ joinspecs;
					c.update(sql);
					c.update("GRANT SELECT ON "+localViewName+" TO iucn");
				
				
			}
		});
	}

}
