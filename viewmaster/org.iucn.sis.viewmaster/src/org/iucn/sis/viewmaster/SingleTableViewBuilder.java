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
	
	public void build(final Connection c) throws DBException {
		c.query("select * from universe", new RowProcessor(){
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
					c.update(String.format("DROP VIEW IF EXISTS %s CASCADE", localViewName));
					StringBuilder columnspecs = new StringBuilder();
					StringBuilder joinspecs = new StringBuilder();
					StringBuilder subcolumnspecs = new StringBuilder();
					// StringBuilder updaters = new StringBuilder();
					
					String joinPrimWith = "field";
					if (sfi > 0) {
						columnspecs.append("s1.recordid, ");
						subcolumnspecs.append("sf.id as recordid, ");
						joinspecs.append("    JOIN field sf ON field.id = sf.parentid AND sf.name = '" + joinTable + "Subfield'\n");
						joinPrimWith = "sf";
					}
					
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
							joinspecs.append("    LEFT JOIN field sf" + count + " ON " + joinPrimWith + ".id = sf"+count+".parentid AND sf"+count+".name = '" + s + "'\n");
						}
						else {
							columnspecs.append("s1."+s);
							subcolumnspecs.append("ff"+count+".value as "+s);
							joinspecs.append("    LEFT JOIN primitive_field pf"+count+" ON pf"+count+".fieldid = "+joinPrimWith+".id AND pf"+count+".name = '" + s + "'\n");
							
							
							if ("foreign_key_list_primitive_field".equals(currentColumns.get(s))) {
								joinspecs.append("    LEFT JOIN "+currentColumns.get(s)+" fi"+count+" ON fi"+count+".id = pf"+count+".id\n");
								joinspecs.append("    LEFT JOIN fk_list_primitive_values ff" + count+ " ON ff"+count+".fk_list_primitive_id = pf"+count+".id\n");
							}
							else
								joinspecs.append("    LEFT JOIN "+currentColumns.get(s)+" ff"+count+" ON ff"+count+".id = pf"+count+".id\n");
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
						"CREATE VIEW "+localViewName+" AS SELECT vw_filter.taxonid, vw_filter.assessmentid, "
						+ columnspecs + "\n"
						+ "FROM vw_filter \n"
						+ "LEFT JOIN ( \n"
						+ "  SELECT vw_filter.assessmentid, "
						+ subcolumnspecs + "\n"
						+ "  FROM vw_filter \n"
						+ "  JOIN field on field.assessmentid = vw_filter.assessmentid AND field.name='"+joinTable+"'\n"
						+ joinspecs
						+ ") s1 ON vw_filter.assessmentid = s1.assessmentid";
					c.update(sql);
					c.update("GRANT SELECT ON "+localViewName+" TO iucn");
					
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
