package org.iucn.sis.viewmaster;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class AccessViewBuilder {
	
	private Connection ec;
	private String schema;
	private String user;
	
	private boolean echo = false;
	private boolean failOnUpdateError = false;
	
	public void setEcho(boolean echo) {
		this.echo = echo;
	}
	
	public void setFailOnUpdateError(boolean failOnUpdateError) {
		this.failOnUpdateError = failOnUpdateError;
	}
	
	public void query(String sql, RowProcessor processor) throws DBException {
		if (echo)
			print(sql);
		ec.query(sql, processor);
	}
	
	public void update(String sql, Object... args) {
		update(String.format(sql, args));
	}
	
	public void update(String sql) {
		try {
			ec.updateOrFail(sql);
		} catch (DBException e){
			printf("Error: Query failed: %s", sql);
			if (failOnUpdateError)
				throw new Error(e);
			else
				GetOut.log(e);
		} finally {
			if (echo)
				print(sql);
		}
	}
	
	public void safeUpdate(String sql, Object... args) {
		safeUpdate(String.format(sql, args));
	}
	
	public void safeUpdate(String sql) {
		try {
			ec.updateOrFail(sql);
		} catch (DBException e){
			//printf("Error: Query failed: %s", sql);
		} finally {
			if (echo)
				print(sql);
		}
	}
	
	public void destroy() throws DBException {
		safeUpdate("DROP SCHEMA %s CASCADE", schema);
	}
	
	public void build(Connection c, final String schema, final String user) throws DBException, IOException {
		this.ec = c;
		this.schema = schema;
		this.user = user;
		
		print("Generating export views...");
		generateExportViews();
		
		printf("Generating utility queries...");
		generateUtilityQueries("vw_published".equals(schema));
		
		printf("Done.");
	}
	
	public void generateExportViews() throws DBException {
		//Export Tables
		query("select * from "+ "public" + ".universe ORDER BY a", new RowProcessor() {
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
					
					printf("Generated system view: %s", currentViewName);
					String localViewName = currentViewName.toUpperCase();
					
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
						"CREATE TABLE "+schema+".\""+localViewName+"\" AS SELECT "+schema+".vw_filter.taxonid, " 
						+ schema + ".vw_filter.assessmentid, "
						+ columnspecs + "\n"
						+ "FROM " + schema + ".vw_filter \n"
						+ "LEFT JOIN ( \n"
						+ "  SELECT " + schema + ".vw_filter.assessmentid, "
						+ subcolumnspecs + "\n"
						+ "  FROM " + schema + ".vw_filter \n"
						+ "  JOIN public.field on public.field.assessmentid = " + schema + ".vw_filter.assessmentid AND public.field.name='"+joinTable+"'\n"
						+ joinspecs + "\n"
						//+ wherespecs
						+ ") s1 ON " + schema + ".vw_filter.assessmentid = s1.assessmentid";
					
					safeUpdate("DROP TABLE IF EXISTS %s.\"%s\"", schema, localViewName);
					safeUpdate(sql);
					safeUpdate("GRANT SELECT ON %s.\"%s\" TO %s", schema, localViewName, user);
					
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
	
	public void generateUtilityQueries(boolean published) throws DBException, IOException {
		runSQLFile("AccessQueries" + (published ? "Published" : "") + ".sql");
	}
	
	public void generateTaxonomyQueries() throws DBException, IOException {
		runSQLFile("AccessQueriesTaxonomy.sql");
	}
	
	private void runSQLFile(String file) throws DBException, IOException {
		for (String sql : new SQLReader(file)) {
			TableInfo info = findTableName(sql);
			if (info == null)
				printf(" - No table name found in query: \n%s", sql);
			
			if (info != null)
				update("DROP %s IF EXISTS %s CASCADE", info.type, info.table);
				
			update(sql.replace("$schema", schema).replace("$user", user));
			
			if (info != null)
				safeUpdate("GRANT SELECT ON %s TO %s", info.table, user);
		}
	}
	
	private TableInfo findTableName(String sql) {
		String type;
		
		int startIndex = sql.indexOf(type = "TABLE");
		if (startIndex < 0)
			startIndex = sql.indexOf(type = "VIEW");
		int endIndex = sql.indexOf(" AS");
		
		if (startIndex < 0 || endIndex < 0)
			return null;
		
		return new TableInfo(type, sql.substring(startIndex + 5, endIndex).trim().replace("$schema", schema));
	}
	
	private void print(String out) {
		GetOut.log(out);
	}
	
	private void printf(String out, Object... args) {
		GetOut.log(out, args);
	}
	
	private static class TableInfo {
		
		public String type, table;
		
		public TableInfo(String type, String table) {
			this.type = type;
			this.table = table;
		}
		
	}

}
