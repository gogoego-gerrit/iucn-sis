package org.iucn.sis.server.api.queries;

import java.text.SimpleDateFormat;
import java.util.Date;

public class H2CannedQueries extends PostgreSQLCannedQueries {

	@Override
	public String getRecentActivity(String mode, Date dateTime, String... parameters) {
		String query;
		String date = new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(dateTime);
		if ("ws".equals(mode)) {
			query = 
				"SELECT DISTINCT u.first_name, u.last_name, u.email, e.created_date, e.reason, " +
				"t.friendly_name, a.id as asm_id, t.id as taxon_id " +
				"FROM assessment_edit ae " +
				"JOIN edit e ON e.id = ae.editid " +
				"JOIN \"user\" u ON u.id = e.userid " +
				"JOIN assessment a ON a.id = ae.assessmentid " +
				"JOIN taxon t ON t.id = a.taxonid " +
				"JOIN working_set_taxon wt ON t.id = wt.taxonid " + 
				"JOIN working_set_subscribe_user w ON w.working_setid = wt.working_setid " + 
				"WHERE w.userid = " + parameters[0] + " AND reason is not null AND created_date > '" + date + "' " + 
				"ORDER BY created_date DESC " +
				"LIMIT 250";
		}
		else if ("mine".equals(mode)) {
			query = 
				"SELECT DiSTINCT u.first_name, u.last_name, u.email, e.created_date, e.reason, " +
				"t.friendly_name, a.id as asm_id, t.id as taxon_id " +
				"FROM assessment_edit ae " +
				"JOIN edit e ON e.id = ae.editid " +
				"JOIN \"user\" u ON u.id = e.userid " +
				"JOIN assessment a ON a.id = ae.assessmentid " +
				"JOIN taxon t ON t.id = a.taxonid " +
				"WHERE u.id = " + parameters[0] + 
				" AND reason is not null AND created_date BETWEEN '" + date + "' AND NOW() " +
				"ORDER BY created_date DESC " +
				"LIMIT 250";
		}
		else {
			query = 
				"SELECT DISTINCT u.first_name, u.last_name, u.email, e.created_date, e.reason, " +
				"t.friendly_name, a.id as asm_id, t.id as taxon_id " +
				"FROM assessment_edit ae " +
				"JOIN edit e ON e.id = ae.editid " +
				"JOIN \"user\" u ON u.id = e.userid " +
				"JOIN assessment a ON a.id = ae.assessmentid " +
				"JOIN taxon t ON t.id = a.taxonid " +
				"WHERE reason is not null AND created_date BETWEEN '" + date + "' AND NOW() " +
				"ORDER BY created_date DESC " +
				"LIMIT 250";
		}
		
		return query;
	}

}
