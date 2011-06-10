package org.iucn.sis.viewmaster;

import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class AdditionalViewBuilder {

	public void build(final Connection c, final String schema, final String user) {
		c.update(sql("DROP VIEW IF EXISTS %s.%s CASCADE", schema, "vw_reference"));
		c.update(sql(
			"CREATE VIEW %s.%s AS " +
			"SELECT %s.vw_filter.taxonid, %s.vw_filter.assessmentid, %s\n" + 
			"FROM %s.vw_filter \n" + 
			"JOIN public.field f ON f.assessmentid = %s.vw_filter.assessmentid\n" +
			"JOIN public.field_reference ON public.field_reference.fieldid = f.id\n" + 
			"JOIN public.reference r ON r.id = public.field_reference.referenceid",
			schema, "vw_reference", schema, schema, "f.id as fieldid, f.name as fieldname, r.*", 
			schema, schema
		));
		c.update(sql("GRANT SELECT ON %s.%s TO %s", schema, "vw_reference", user));
		
		c.update(sql("DROP VIEW IF EXISTS %s.%s CASCADE", "lookups", "REGIONINFORMATION_REGIONSLOOKUP"));
		c.update(sql("CREATE VIEW %s.%s AS " +
			"SELECT public.region.id AS \"ID\", CAST (public.region.id AS varchar(255)) AS \"NAME\", public.region.name AS \"LABEL\" FROM public.region", 
			"lookups", "REGIONINFORMATION_REGIONSLOOKUP"));
		c.update(sql("GRANT SELECT ON %s.%s TO %s", "lookups", "REGIONINFORMATION_REGIONSLOOKUP", "PUBLIC"));
		
		for (String field : new String[] { CanonicalNames.RedListAssessors, CanonicalNames.RedListEvaluators, CanonicalNames.RedListContributors, CanonicalNames.RedListFacilitators }) {
			String table = field.toUpperCase() + "_VALUELOOKUP";
			c.update(sql("DROP VIEW IF EXISTS %s.%s CASCADE", "lookups", table));
			c.update(sql("CREATE VIEW %s.%s AS " +
				"SELECT public.\"user\".id AS \"ID\", CAST (public.\"user\".id AS varchar(255)) AS \"NAME\", public.\"user\".username AS \"LABEL\" FROM public.\"user\"", 
				"lookups", table));
			c.update(sql("GRANT SELECT ON %s.%s TO %s", "lookups", table, "PUBLIC"));
		}
		
		c.update(sql("DROP VIEW IF EXISTS %s.%s CASCADE", "lookups", "THREATS_VIRUSLOOKUP"));
		c.update(sql("CREATE VIEW %s.%s AS " +
			"SELECT public.virus.id AS \"ID\", CAST(public.virus.id AS varchar(255)) AS \"NAME\", public.virus.name AS \"LABEL\" FROM public.virus", 
			"lookups", "THREATS_VIRUSLOOKUP"));
		c.update(sql("GRANT SELECT ON %s.%s TO %s", "lookups", "THREATS_VIRUSLOOKUP", "PUBLIC"));
		
		c.update(sql("DROP VIEW IF EXISTS %s.%s CASCADE", "lookups", "THREATS_IASLOOKUP"));
		c.update(sql("CREATE VIEW %s.%s AS " + 
			"SELECT public.taxon.id AS \"ID\", public.taxon.name AS \"NAME\", public.taxon.friendly_name AS \"LABEL\" FROM public.taxon WHERE taxon.taxon_levelid = 7 AND taxon.state = 0", 
			"lookups", "THREATS_IASLOOKUP"));
		c.update(sql("GRANT SELECT ON %s.%s TO %s", "lookups", "THREATS_IASLOOKUP", "PUBLIC"));
		
	}
	
	private String sql(String template, Object... args) {
		String out = String.format(template, args);
		GetOut.log(out);
		return out;
	}
	
}
