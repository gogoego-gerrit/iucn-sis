package org.iucn.sis.viewmaster;

import java.util.Map;
import java.util.TreeMap;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class SingleTableViewBuilder {
	
	public void build(final Connection c, final String schema, final String user) throws DBException {
		c.query("select * from "+ schema + ".universe", new RowProcessor(){
			String currentViewName = CanonicalNames.AOO;
			Map<String,String> currentColumns = new TreeMap<String,String>();
			
			@Override
			public void process(Row row) {
				String newViewName = row.get("a").getString();
				if(!newViewName.equals(currentViewName)){
					String joinTable = FriendlyNameFactory.get(currentViewName);
					int sfi = currentViewName.toUpperCase().indexOf("SUBFIELD");
					if (sfi > 0)
						joinTable = FriendlyNameFactory.get(currentViewName.substring(0, sfi));
					
					GetOut.log("New view: %s", currentViewName);
					String localViewName = "vw_" + currentViewName;
					c.update(String.format("DROP VIEW IF EXISTS %s.%s CASCADE", schema, localViewName));
					StringBuilder columnspecs = new StringBuilder();
					StringBuilder joinspecs = new StringBuilder();
					StringBuilder subcolumnspecs = new StringBuilder();
					StringBuilder wherespecs = new StringBuilder("    WHERE ");
					// StringBuilder updaters = new StringBuilder();
					
					String joinPrimWith = "field";
					if (sfi > 0) {
						columnspecs.append("s1.recordid, ");
						subcolumnspecs.append("sf.id as recordid, ");
						joinspecs.append("    JOIN public.field sf ON public.field.id = sf.parentid AND sf.name = '" + joinTable + "Subfield'\n");
						wherespecs.append("sf.name = '" + joinTable + "Subfield'");
						joinPrimWith = "sf";
					}
					
					boolean firstWhere = true;
					boolean first = true;
					int count = 0;
					for(String s : currentColumns.keySet()){
						count++;
						if(!first){
							columnspecs.append(", ");
							subcolumnspecs.append(", ");
						}
						first = false;
						
						if ("field".equals(currentColumns.get(s))) {
							columnspecs.append("s1."+s);
							subcolumnspecs.append("sf" + count + ".id as " + s);
							joinspecs.append("    LEFT JOIN public.field sf" + count + " ON " + joinPrimWith + ".id = sf"+count+".parentid\n");
							if (!firstWhere)
								wherespecs.append(" AND ");
							wherespecs.append("sf"+count+".name = '" + s + "'");
						}
						else {
							columnspecs.append("s1."+s);
							subcolumnspecs.append("ff"+count+".value as "+s);
							joinspecs.append("    LEFT JOIN public.primitive_field pf"+count+" ON pf"+count+".fieldid = "+joinPrimWith+".id\n");
							if (!firstWhere)
								wherespecs.append(" AND ");
							wherespecs.append("pf"+count+".name = '" + s + "'");
							
							if ("foreign_key_list_primitive_field".equals(currentColumns.get(s))) {
								joinspecs.append("    LEFT JOIN public."+currentColumns.get(s)+" fi"+count+" ON fi"+count+".id = pf"+count+".id\n");
								joinspecs.append("    LEFT JOIN public.fk_list_primitive_values ff" + count+ " ON ff"+count+".fk_list_primitive_id = pf"+count+".id\n");
							}
							else
								joinspecs.append("    LEFT JOIN public."+currentColumns.get(s)+" ff"+count+" ON ff"+count+".id = pf"+count+".id\n");
						}
						/*
						updaters.append(
							  "  IF r."+s+" IS NOT NULL\n"
							+ "  THEN\n"
							+ "    primitive_id = getPrimitiveId(r.assessmentid,'"+currentViewName.toUpperCase()+"','"+currentColumns.get(s)+"','"+s+"');\n"
							+ "    EXECUTE 'UPDATE "+currentColumns.get(s)+" SET value=$1 WHERE id=$2'\n"
							+ "      USING r."+s+", primitive_id;\n"
							+ "  ELSE\n"
							+ "    RAISE NOTICE 'handling empty range';\n"
							+ "    primitive_id = getPrimitiveId(r.assessmentid,'"+currentViewName.toUpperCase()+"','"+currentColumns.get(s)+"','"+s+"');\n"
							+ "    EXECUTE 'UPDATE "+currentColumns.get(s)+" SET value=$1 WHERE id=$2'\n"
							+ "      USING '', primitive_id;\n"
							+ "  END IF;\n"
						);
						*/
					}
					String sql = 
						"CREATE VIEW "+schema+"."+localViewName+" AS SELECT "+schema+".vw_filter.taxonid, "+schema+".vw_filter.assessmentid, "
						+ columnspecs + "\n"
						+ "FROM " + schema + ".vw_filter \n"
						+ "LEFT JOIN ( \n"
						+ "  SELECT " + schema + ".vw_filter.assessmentid, "
						+ subcolumnspecs + "\n"
						+ "  FROM " + schema + ".vw_filter \n"
						+ "  JOIN public.field on public.field.assessmentid = " + schema + ".vw_filter.assessmentid AND public.field.name='"+joinTable+"'\n"
						+ joinspecs + "\n"
						+ wherespecs
						+ ") s1 ON " + schema + ".vw_filter.assessmentid = s1.assessmentid";
					c.update(sql);
					c.update("GRANT SELECT ON "+schema+"."+localViewName+" TO " + user);
					
					/*
					c.update("DROP RULE IF EXISTS up ON "+localViewName);
					c.update("DROP FUNCTION IF EXISTS "+localViewName+"_up_f(r "+localViewName+")");
					c.update("DROP FUNCTION IF EXISTS "+localViewName+"up_f(r "+localViewName+")");
					sql = 
						"CREATE FUNCTION "+localViewName+"_up_f(r "+localViewName+") RETURNS void as $$\n"
						+ "DECLARE \n"
						+ "  primitive_id int;\n"
						+ "BEGIN \n"
						+ updaters
						+ "END \n"
						+ "$$ LANGUAGE plpgsql\n";
					c.update(sql);
					sql =
						"CREATE RULE up\n"
						+ "AS ON UPDATE to "+localViewName+"\n"
						+ "DO INSTEAD (SELECT "+localViewName+"_up_f(new));\n";
					c.update(sql);
					*/
					currentViewName = newViewName;
					currentColumns.clear();
				}
				String name = row.get("c").getString();
				String where = row.get("d").getString();
				if("fk_primitive_field".equals(where))
					where = "foreign_key_primitive_field";
				if("fk_list_primitive_field".equals(where))
					where = "foreign_key_list_primitive_field";
				if(!"N/A".equals(name)) currentColumns.put(name,where);
			}
		});
	}

}
