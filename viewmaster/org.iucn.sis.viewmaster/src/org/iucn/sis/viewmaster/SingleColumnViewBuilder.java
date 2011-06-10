package org.iucn.sis.viewmaster;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;

public class SingleColumnViewBuilder {
	
	public void build(final Connection c, final String schema, final String user) throws DBException {
		c.query("select * from " + schema + ".universe", new RowProcessor(){
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
					
				c.update(String.format("DROP VIEW IF EXISTS %s.%s CASCADE", schema, localViewName));
				StringBuilder columnspecs = new StringBuilder();
				StringBuilder joinspecs = new StringBuilder();
				
				if ("field".equals(type)) {
					columnspecs.append("sf.id as recordid");
					joinspecs.append("    JOIN public.field sf ON public.field.id = sf.parentid AND sf.name = '" + joinTable + "Subfield'");
				}
				else {
					String joinPrimWith = "field";
					if (sfi > 0) {
						columnspecs.append("sf.id as recordid, ");
						joinspecs.append("    JOIN public.field sf ON field.id = sf.parentid AND sf.name = '" + joinTable + "Subfield'\n");
						joinPrimWith = "sf";
					}
					columnspecs.append("ff.value");
					joinspecs.append("    JOIN public.primitive_field pf ON pf.fieldid = "+joinPrimWith+".id AND pf.name = '" + name + "'\n");
					if ("foreign_key_list_primitive_field".equals(type)) {
						joinspecs.append("    JOIN public."+type+" fi ON fi.id = pf.id\n");
						joinspecs.append("    JOIN public.fk_list_primitive_values ff ON ff.fk_list_primitive_id = pf.id\n");
					}
					else
						joinspecs.append("    JOIN public."+type+" ff ON ff.id = pf.id\n");
				}
					
				String sql = 
					"CREATE VIEW "+schema+"."+localViewName+" AS SELECT " + schema + ".vw_filter.taxonid, "+schema+".vw_filter.assessmentid, "
					+ columnspecs + "\n"
					+ "FROM " + schema + ".vw_filter \n"
					+ "  JOIN public.field on public.field.assessmentid = " + schema + ".vw_filter.assessmentid AND public.field.name='"+joinTable+"'\n"
					+ joinspecs;
					c.update(sql);
					c.update("GRANT SELECT ON "+schema+"."+localViewName+" TO " + user);
				
				
			}
		});
	}

}
