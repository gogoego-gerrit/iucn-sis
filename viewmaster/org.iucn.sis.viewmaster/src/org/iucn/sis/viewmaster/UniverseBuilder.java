package org.iucn.sis.viewmaster;

import org.gogoego.util.db.DBException;
import org.gogoego.util.db.RowProcessor;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.shared.Row;
import org.gogoego.util.getout.GetOut;

public class UniverseBuilder {
	
	public void build(final Connection c, final Connection l) throws DBException {
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
									String formattedRelname = FriendlyNameFactory.get(relname);
									String vb = "";
									if (fieldRow.getColumns().get(0) != null) vb = fieldRow.getColumns().get(0).getString();
									String vc = "";
									if (fieldRow.getColumns().get(1) != null) vc = fieldRow.getColumns().get(1).getString();
									String vd = "";
									if (fieldRow.getColumns().get(2) != null) vd = fieldRow.getColumns().get(2).getString();
									c.update(String.format("INSERT INTO universe (a,b,c,d) VALUES ('%s','%s','%s','%s')",
											formattedRelname, vb, vc, vd));
								}
							});
						} catch (DBException dbx) {
							GetOut.log(dbx);
						}
					}
				}
			}
		});
	}

}
