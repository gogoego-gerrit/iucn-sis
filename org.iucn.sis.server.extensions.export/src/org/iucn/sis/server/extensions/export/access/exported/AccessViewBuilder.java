package org.iucn.sis.server.extensions.export.access.exported;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.Hibernate;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.util.DynamicWriter;

public class AccessViewBuilder extends DynamicWriter {
	
	private final ExecutionContext ec;
	private final WorkingSet ws;
	private final String schema;
	
	private boolean failOnUpdateError = false;
	private boolean ignoreWorkingSetRestrictions = false;
	
	public AccessViewBuilder(WorkingSet ws, ExecutionContext ec) {
		this(ws, ec, "access_" + ws.getId() + "_" + new Date().getTime());
	}
	
	public AccessViewBuilder(WorkingSet ws, ExecutionContext ec, String schema) {
		super();
		this.ws = ws;
		this.ec = ec;
		this.schema = schema;	
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setFailOnUpdateError(boolean failOnUpdateError) {
		this.failOnUpdateError = failOnUpdateError;
	}
	
	public void setIgnoreWorkingSetRestrictions(boolean ignoreWorkingSetRestrictions) {
		this.ignoreWorkingSetRestrictions = ignoreWorkingSetRestrictions;
	}
	
	public void query(String sql, RowProcessor processor) throws DBException {
		ec.doQuery(sql, processor);
	}
	
	public void update(String sql, Object... args) {
		update(String.format(sql, args));
	}
	
	public void update(String sql) {
		try {
			ec.doUpdate(sql);
		} catch (DBException e){
			printf("Error: Query failed: %s", sql);
			if (failOnUpdateError)
				throw new Error(e);
			else
				e.printStackTrace();
		}
	}
	
	public void safeUpdate(String sql, Object... args) {
		safeUpdate(String.format(sql, args));
	}
	
	public void safeUpdate(String sql) {
		try {
			ec.doUpdate(sql);
		} catch (DBException e){
			//printf("Error: Query failed: %s", sql);
		}
	}
	
	public void destroy() throws DBException {
		safeUpdate("DROP SCHEMA %s CASCADE", schema);
	}
	
	public void build() throws DBException, IOException {
		generateSchema();
		
		generateFilter();
		
		print("Generating basic tables...");
		
		//Required tables
		for (String tbl : new String[] { "assessment", "taxon" }) {
			print(tbl + "...");
			update(String.format("CREATE VIEW %s.%s AS \nSELECT %s.* FROM public.%s",
				schema, tbl, tbl, tbl));
		}
		
		print("Generating export views...");
		
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
					update(sql);
					
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
		
		generateTaxonListing();
		generateTaxonomyQueries();
		
		printf("Generating utility queries...");
		generateUtilityQueries();
		
		printf("Done.");
	}
	
	public void generateSchema() {
		print("Generating schema for working set...");
		
		safeUpdate(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schema));
		update(String.format("CREATE SCHEMA %s", schema));
	}
	
	public void generateFilter() {
		print("Generating filter...");
		
		if (ignoreWorkingSetRestrictions)
			createFilterBasedOnTaxa();
		else
			createFilterBasedOnWorkingSet();	
	}
	
	public void generateTaxonListing() {
		printf("Generating taxonomic hierarchy...");
		
		update("CREATE TABLE %s.vw_filter_taxa ( taxonid integer );", schema);
		HashSet<Integer> seen = new HashSet<Integer>();
		Hibernate.initialize(ws.getTaxon());
		for (Taxon taxon : ws.getTaxon())
			insertTaxa(taxon, seen);
	}
	
	private void insertTaxa(Taxon taxon, HashSet<Integer> seen) {
		if (taxon != null && !seen.contains(taxon.getId())) {
			update("INSERT INTO %s.vw_filter_taxa (taxonid) VALUES (%s);", schema, taxon.getId());
			
			seen.add(taxon.getId());
			
			insertTaxa(taxon.getParent(), seen);
		}
	}
	
	public void createFilterBasedOnWorkingSet() {
		StringBuilder where = new StringBuilder();
		where.append("WHERE a.state = 0 AND taxon.state = 0 AND wt.working_setid = %s");
		if (ws.getRelationship().getId() != Relationship.ALL_ID) {
			String joinTerm = ws.getRelationship().getName().toUpperCase();
			where.append(" AND (");
			for (Iterator<Region> iter = ws.getRegion().iterator(); iter.hasNext();) {
				where.append("region.value = " + iter.next().getId());
				if (iter.hasNext())
					where.append(" " + joinTerm + " ");
			}
			where.append(")");
		}
		where.append(" AND (");
		for (Iterator<AssessmentType> iter = ws.getAssessmentTypes().iterator(); iter.hasNext(); ) {
			where.append("a.assessment_typeid = " + iter.next().getId());
			if (iter.hasNext())
				where.append(" OR ");
		}
		where.append(")");
		
		update("CREATE TABLE %s.vw_filter AS " +
			"SELECT DISTINCT a.taxonid, a.id as assessmentid " +
			"FROM public.assessment a " +
			"JOIN public.taxon ON a.taxonid = taxon.id " +
			"JOIN public.working_set_taxon wt ON wt.taxonid = a.taxonid " +
			"JOIN public.field f ON a.id = f.assessmentid AND f.name = 'RegionInformation' " +
			"JOIN public.primitive_field pf ON f.id = pf.fieldid AND pf.name = 'regions' " +
			"JOIN public.foreign_key_list_primitive_field fk ON pf.id = fk.id " +
			"JOIN public.fk_list_primitive_values region ON fk.id = region.fk_list_primitive_id " + 
			where, schema, ws.getId());
	}
	
	public void createFilterBasedOnTaxa() {
		update("CREATE TABLE %s.vw_filter AS " +
			"SELECT DISTINCT a.taxonid, a.id as assessmentid " +
			"FROM public.assessment a " +
			"JOIN public.taxon ON a.taxonid = taxon.id " +
			"JOIN public.working_set_taxon wt ON wt.taxonid = a.taxonid " +
			"WHERE a.state = 0 AND taxon.state = 0 AND wt.working_setid = %s", schema, ws.getId());
	}
	
	public void generateUtilityQueries() throws DBException, IOException {
		runSQLFile("AccessQueries.sql");
	}
	
	public void generateTaxonomyQueries() throws DBException, IOException {
		runSQLFile("AccessQueriesTaxonomy.sql");
	}
	
	private void runSQLFile(String file) throws DBException, IOException {
		for (String sql : new SQLReader(file))
			update(sql.replace("$schema", schema));
	}
	
	private void print(String out) {
		write(out);
	}
	
	private void printf(String out, Object... args) {
		write(String.format(out, args));
	}

}
