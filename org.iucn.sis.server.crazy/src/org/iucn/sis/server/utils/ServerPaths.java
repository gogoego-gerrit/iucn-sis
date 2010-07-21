package org.iucn.sis.server.utils;

import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;

public class ServerPaths {

	public static final String USER_ASSESSMENT_STATUS = "user_status";
	public static final String DRAFT_ASSESSMENT_STATUS = "draft_status";
	public static final String PUBLISHED_ASSESSMENT_STATUS = "published_status";
	public static final String TAXON = "taxon";

	public static String getFieldURL(String fieldName) {
		return "/browse/docs/fields/" + fieldName + ".xml";
	}

	public static String getLastTaxomaticOperationPath() {
		return "/browse/taxonomy/lastOperation.xml";
	}
	
	/**
	 * Fetches the appropriate URI to save this assessment to. Username parameter can be null
	 * if not sending in a User assessment.
	 * 
	 * @param assessment
	 * @param username
	 * @return the URI to save this assessment to
	 */
	public static String getPathForAssessment(AssessmentData assessment, String username) {
		if( assessment.getType().equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
			return getPublishedAssessmentURL(assessment.getAssessmentID());
		} else if( assessment.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
			return getDraftAssessmentURL(assessment.getAssessmentID());
		} else {
			return getUserAssessmentUrl(username, assessment.getAssessmentID());
		}
	}
	
	public static String getDraftAssessmentRootURL() {
		return "/drafts/";
	}

	public static String getDraftAssessmentRootURL(String id) {
		String numID = id;
		if (id.indexOf("_") > 0)
			numID = id.substring(0, id.indexOf("_"));
		
		return getDraftAssessmentRootURL() + FilenameStriper.getIDAsStripedPath(numID) + "_/";
	}
	
	public static String getDraftAssessmentURL(String id) {
		String numID = id;
		if (id.indexOf("_") > 0)
			numID = id.substring(0, id.indexOf("_"));
		
		return getDraftAssessmentRootURL() + FilenameStriper.getIDAsStripedPath(numID) + "_/" + id + ".xml";
	}

	public static String getPrivateWorkingSetFolderURL(String username) {
		return "/users/" + username + "/workingsets";
	}

	public static String getPrivateWorkingSetURL(String username, String id) {
		return getPrivateWorkingSetFolderURL(username) + "/" + id + ".xml";
	}

	public static String getPublicWorkingSetFolderURL() {
		return "/workingsets";
	}

	public static String getPublicWorkingSetURL(String id) {
		return (getPublicWorkingSetFolderURL() + "/" + FilenameStriper.getIDAsStripedPath(id) + ".xml");
	}

	public static String getPublishedAssessmentURL() {
		return "/browse/assessments/";
	}

	public static String getPublishedAssessmentURL(String id) {
		id = id.replaceAll("(\\.xml)", "");
		return getPublishedAssessmentURL() + FilenameStriper.getIDAsStripedPath(id) + ".xml";
	}

	public static String getSpeciesReportPath() {
		return "/reports/speciesReport";
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

	public static String getURLForTaxa(String id) {
		id = id.replace("(\\.xml)", "");
		String stripedID = FilenameStriper.getIDAsStripedPath(id);
		return getURLForTaxaFolder() + stripedID + ".xml";
	}

	public static String getURLForTaxaFolder() {
		return "/browse/nodes/";
	}

	public static String getUserAssessmentUrl(String username) {
		return "/users/" + username + "/assessments/";
	}

	public static String getUserAssessmentUrl(String username, String id) {
		id = id.replace("(\\.xml)", "");
		return getUserAssessmentUrl(username) + FilenameStriper.getIDAsStripedPath(id) + ".xml";
	}
	
	public static String getNotesRootUrl() {
		return "/notes";
	}
}
