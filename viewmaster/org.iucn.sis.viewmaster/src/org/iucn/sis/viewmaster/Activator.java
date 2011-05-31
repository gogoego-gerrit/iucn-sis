package org.iucn.sis.viewmaster;

import static org.gogoego.util.db.fluent.Statics.configure;

import java.util.Map;
import java.util.TreeMap;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		
		final Connection c = configure(
				"sistest_clean_20110525",
				"**URL**",
				"**USERNAME**",
				"**PASSWORD**");

		final Connection l = configure(
				"sis_lookups",
				"**URL**",
				"**USERNAME**",
				"**PASSWORD**");
		
		c.update("DROP TABLE IF EXISTS universe");
		c.update("CREATE TABLE universe (a VARCHAR(255), b VARCHAR(255), c VARCHAR(255), d VARCHAR(255));");
		
		try{
			c.update("CREATE TABLE vw_filter (taxonid INTEGER, assessmentid INTEGER);");
			c.update("DELETE FROM vw_filter");
			c.update("insert into vw_filter select taxonid, id from assessment;");
			c.update("create index taxonid on vw_filter (taxonid);");
			c.update("create index assessmentid on vw_filter (assessmentid);");
			c.update("create index taxonid_assessmentid on vw_filter (taxonid, assessmentid);");
			c.update("GRANT SELECT ON vw_filter TO iucn");
		} catch (Exception ignored) {};
		
		l.query("SELECT CAST(relname as varchar(255)) as relname FROM pg_stat_user_tables WHERE schemaname='public'", new RowProcessor(){
			@Override
			public void process(Row row) {
				if(row.get("relname")!=null){
					final String relname = row.get("relname").getString();
					if(!relname.endsWith("LOOKUP")){
						GetOut.log(relname);
						try{
							String q = "SELECT * from \""+relname+"\"";
							GetOut.log(q);
							l.query(q, new RowProcessor(){
								public void process(Row fieldRow) {
									String vb = "";
									if (fieldRow.getColumns().get(0) != null) vb = fieldRow.getColumns().get(0).getString();
									String vc = "";
									if (fieldRow.getColumns().get(1) != null) vc = fieldRow.getColumns().get(1).getString();
									String vd = "";
									if (fieldRow.getColumns().get(2) != null) vd = fieldRow.getColumns().get(2).getString();
									c.update(String.format("INSERT INTO universe (a,b,c,d) VALUES ('%s','%s','%s','%s')",
											relname, vb, vc, vd));
								}
							});
						} catch (DBException dbx) {
							GetOut.log(dbx);
						}
					}
				}
			}
		});


		/*
		c.update("DROP FUNCTION IF EXISTS getPrimitiveId(a int, f varchar(255), t varchar(255), n varchar(255)) CASCADE");

		BufferedReader br = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("getPrimitiveId.sql")
		));
		String in = "";
		StringBuilder sql = new StringBuilder();
		while(in != null){
			sql.append(in+"\n");
			in = br.readLine();
		}
		br.close();
		
		c.update(sql.toString());
		*/

		c.query("select * from universe", new RowProcessor(){
			String currentViewName = "aoo";
			Map<String,String> currentColumns = new TreeMap<String,String>();
			
			@Override
			public void process(Row row) {
				String newViewName = row.get("a").getString();
				if(!newViewName.equals(currentViewName)){
					GetOut.log("New view: %s", currentViewName);
					String localViewName = "vw_" + currentViewName;
					c.update(String.format("DROP VIEW IF EXISTS %s CASCADE", localViewName));
					StringBuilder columnspecs = new StringBuilder();
					StringBuilder joinspecs = new StringBuilder();
					StringBuilder subcolumnspecs = new StringBuilder();
					// StringBuilder updaters = new StringBuilder();
					boolean first = true;
					int count = 0;
					for(String s : currentColumns.keySet()){
						count++;
						if(!first){
							columnspecs.append(", ");
							subcolumnspecs.append(", ");
						}
						first = false;
						columnspecs.append("s1."+s);
						subcolumnspecs.append("ff"+count+".value as "+s);
						joinspecs.append("    JOIN primitive_field pf"+count+" ON pf"+count+".fieldid = field.id\n");
						joinspecs.append("    JOIN "+currentColumns.get(s)+" ff"+count+" ON ff"+count+".id = pf"+count+".id\n");
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
						+ "  JOIN field on field.assessmentid = vw_filter.assessmentid AND field.name='"+currentViewName.toUpperCase()+"'\n"
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
				//FIXME this is mishandled but allows view to be created
				if("fk_list_primitive_field".equals(where))
					where = "foreign_key_primitive_field";
				//FIXME this is mishandled but allows view to be created
				if("field".equals(where))
					where = "string_primitive_field";
				if(!"N/A".equals(name)) currentColumns.put(name,where);
			}
		});
		
	}

	public void stop(BundleContext context) throws Exception {
	}

}
