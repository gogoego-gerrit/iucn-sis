package org.iucn.sis.viewmaster;

import java.util.Map;
import java.util.TreeMap;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class AccessViewBuilder {
	
	public void build(final Connection c) throws DBException {
		final String schema = "access";
		final String user = "public";
		
		GetOut.log("Building table");
		
		c.update(String.format("DROP SCHEMA IF EXISTS %s", schema));
		c.update(String.format("CREATE SCHEMA %s", schema));
		c.update(String.format("GRANT USAGE ON SCHEMA %s TO %s", schema, user));
		
		GetOut.log("Creating required tables");
		//Required tables
		for (String tbl : new String[] { "assessment", "taxon" }) {
			GetOut.log(tbl + "...");
			c.update(String.format("DROP VIEW IF EXISTS %s.%s CASCADE", schema, tbl));
			c.update(String.format("CREATE VIEW %s.%s AS \nSELECT * FROM public.%s", schema, tbl, tbl));
			c.update(String.format("GRANT SELECT ON %s.%s TO %s", schema, tbl, user));
		}
		
		GetOut.log("Creating export tables");
		//Export Tables
		c.query("select * from "+ "public" + ".universe", new RowProcessor(){
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
					String localViewName = "export_" + currentViewName.toUpperCase();
					c.update(String.format("DROP VIEW IF EXISTS %s.\"%s\" CASCADE", schema, localViewName));
					StringBuilder columnspecs = new StringBuilder();
					StringBuilder joinspecs = new StringBuilder();
					StringBuilder subcolumnspecs = new StringBuilder();
					//StringBuilder wherespecs = new StringBuilder("    WHERE ");
					
					boolean firstWhere = true;
					
					String joinPrimWith = "field";
					if (sfi > 0) {
						columnspecs.append("s1.recordid, ");
						subcolumnspecs.append("sf.id as recordid, ");
						joinspecs.append("    JOIN public.field sf ON public.field.id = sf.parentid AND sf.name = '" + joinTable + "Subfield'\n");
						//wherespecs.append("sf.name = '" + joinTable + "Subfield'");
						joinPrimWith = "sf";
						firstWhere = false;
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
							joinspecs.append("    LEFT JOIN public.field sf" + count + " ON " + 
									joinPrimWith + ".id = sf"+count+".parentid AND sf" + 
									count + ".name = '" + s + "'\n");
							/*if (!firstWhere)
								wherespecs.append(" AND ");
							wherespecs.append("sf"+count+".name = '" + s + "'");*/
						}
						else {
							columnspecs.append("s1."+s);
							subcolumnspecs.append("ff"+count+".value as "+s);
							joinspecs.append("    LEFT JOIN public.primitive_field pf"+count+
									" ON pf"+count+".fieldid = "+joinPrimWith+".id AND pf" + 
									count + ".name = '" + s + "'\n");
							/*if (!firstWhere)
								wherespecs.append(" AND ");
							wherespecs.append("pf"+count+".name = '" + s + "'");*/
							
							if ("foreign_key_list_primitive_field".equals(currentColumns.get(s))) {
								joinspecs.append("    LEFT JOIN public."+currentColumns.get(s)+" fi"+count+" ON fi"+count+".id = pf"+count+".id\n");
								joinspecs.append("    LEFT JOIN public.fk_list_primitive_values ff" + count+ " ON ff"+count+".fk_list_primitive_id = pf"+count+".id\n");
							}
							else
								joinspecs.append("    LEFT JOIN public."+currentColumns.get(s)+" ff"+count+" ON ff"+count+".id = pf"+count+".id\n");
						}
						firstWhere = false;
					}
					String sql = 
						"CREATE VIEW "+schema+".\""+localViewName+"\" AS SELECT "+"public"+".assessment.taxonid, "+"public"+".assessment.id as assessmentid, "
						+ columnspecs + "\n"
						+ "FROM " + "public" + ".assessment \n"
						+ "LEFT JOIN ( \n"
						+ "  SELECT " + "public" + ".assessment.id as assessmentid, "
						+ subcolumnspecs + "\n"
						+ "  FROM " + "public" + ".assessment \n"
						+ "  JOIN public.field on public.field.assessmentid = " + "public" + ".assessment.id AND public.field.name='"+joinTable+"'\n"
						+ joinspecs + "\n"
						//+ wherespecs
						+ ") s1 ON " + "public" + ".assessment.id = s1.assessmentid";
					c.update(sql);
					c.update("GRANT SELECT ON "+schema+".\""+localViewName+"\" TO " + user);
					
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
