package org.iucn.sis.shared.helpers;

import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.Assessment;


public class ServerPaths {

	public static final String USER_ASSESSMENT_STATUS = "user_status";
	public static final String DRAFT_ASSESSMENT_STATUS = "draft_status";
	public static final String PUBLISHED_ASSESSMENT_STATUS = "published_status";
	public static final String TAXON = "taxon";

	public static String getLastTaxomaticOperationPath() {
		return "/browse/taxonomy/lastOperation.xml";
	}


	public static String getWorkingSetRootPath() {
		return "/workingsets";
	}

	public static String getPublicWorkingSetURL(String id) {
		return (getWorkingSetRootPath() + "/" + FilenameStriper.getIDAsStripedPath(id) + ".xml");
	}

	public static String getAssessmentRootURL() {
		return "/assessments";
	}

	public static String getAssessmentUrl(String id) {
		id = id.replaceAll("(\\.xml)", "");
		return getAssessmentUrl(Integer.valueOf(id));
	}
	
	public static String getAssessmentUrl(Integer id) {
		
		return getAssessmentRootURL() + "/" + FilenameStriper.getIDAsStripedPath(id) + ".xml";
	}

	public static String getSpeciesReportPath() {
		return "/reports/speciesReport";
	}
	
	public static String getAssessmentURL(Assessment assessment) {
		return getAssessmentUrl(assessment.getId()+"");
		
	}

	/**
	 * The reportID is the id+user
	 * 
	 * @param reportID
	 * @return
	 */
	public static String getSpeciesReportURL(String reportID) {
		return "/reports/speciesReport/" + reportID + ".zip";
	}

	public static String getTaxonomyByNameURL() {
		return "/browse/taxonomy/taxonomyByName.xml";
	}

	public static String getTaxonomyDocURL() {
		return "/browse/taxonomy/taxonomy.xml";
	}

	public static String getTaxonURL(Integer id) {
		String stripedID = FilenameStriper.getIDAsStripedPath(id);
		return getTaxonRootURL() + "/" + stripedID + ".xml";
	}
	
	public static String getTaxonURL(String id) {
		id = id.replace("(\\.xml)", "");
		return getTaxonURL(Integer.valueOf(id));
	}

	public static String getTaxonRootURL() {
		return "/taxa";
	}
	
	public static String getUserRootPath() {
		return "/users";
	}
	
	public static String getUserPath(String username) {
		return getUserRootPath() + "/" + username;
	}
	
	public static String getNotesRootUrl() {
		return "/notes";
	}
}
