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
				
				String joinTable = tableName;
				
				if("fk_primitive_field".equals(type))
					type = "foreign_key_primitive_field";
				if("fk_list_primitive_field".equals(type))
					type = "foreign_key_list_primitive_field";
				
				int sfi = tableName.toUpperCase().indexOf("SUBFIELD");
				if (sfi > 0)
					joinTable = FriendlyNameFactory.get(tableName.substring(0, sfi));
				
				GetOut.log("New view: %s.%s", tableName, name);
				String localViewName = "vw_" + tableName + "_" + name;
					
				c.update(String.format("DROP VIEW IF EXISTS %s CASCADE", localViewName));
				StringBuilder columnspecs = new StringBuilder();
				StringBuilder joinspecs = new StringBuilder();
				
				if ("field".equals(type)) {
					columnspecs.append("sf.id as recordid");
					joinspecs.append("    JOIN field sf ON field.id = sf.parentid AND sf.name = '" + joinTable + "Subfield'");
				}
				else {
					String joinPrimWith = "field";
					if (sfi > 0) {
						columnspecs.append("sf.id as recordid, ");
						joinspecs.append("    JOIN field sf ON field.id = sf.parentid AND sf.name = '" + joinTable + "Subfield'\n");
						joinPrimWith = "sf";
					}
					columnspecs.append("ff.value");
					joinspecs.append("    JOIN primitive_field pf ON pf.fieldid = "+joinPrimWith+".id AND pf.name = '" + name + "'\n");
					if ("foreign_key_list_primitive_field".equals(type)) {
						joinspecs.append("    JOIN "+type+" fi ON fi.id = pf.id\n");
						joinspecs.append("    JOIN fk_list_primitive_values ff ON ff.fk_list_primitive_id = pf.id\n");
					}
					else
						joinspecs.append("    JOIN "+type+" ff ON ff.id = pf.id\n");
				}
					
				String sql = 
					"CREATE VIEW "+localViewName+" AS SELECT vw_filter.taxonid, vw_filter.assessmentid, "
					+ columnspecs + "\n"
					+ "FROM vw_filter \n"
					+ "  JOIN field on field.assessmentid = vw_filter.assessmentid AND field.name='"+joinTable+"'\n"
					+ joinspecs;
					c.update(sql);
					c.update("GRANT SELECT ON "+localViewName+" TO iucn");
				
				
			}
		});
	}

}
