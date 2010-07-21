package org.iucn.sis.server.utils;

import org.iucn.sis.shared.data.assessments.AssessmentData;

public class ServerPathsv2 {

	public static final String USER_ASSESSMENT_STATUS = "user_status";
	public static final String DRAFT_ASSESSMENT_STATUS = "draft_status";
	public static final String PUBLISHED_ASSESSMENT_STATUS = "published_status";
	public static final String TAXON = "taxon";

	public static String getFieldURL(String fieldName) {
		return "/browse/docs/fields/" + fieldName + ".xml";
	}

	public static String getDraftAssessmentUrl() {
		return "/drafts/";
	}

	public static String getLastTaxomaticOperationPath() {
		return "/browse/taxonomy/lastOperation.xml";
	}

	public static String getPathForSpeciesDraftAssessments(String speciesID) {
		return getDraftAssessmentUrl() + FilenameStriper.getIDAsStripedPath(speciesID) + "_/";
	}
	
	public static String getPathForDraftAssessment(AssessmentData assessment) {
		return getDraftAssessmentUrl() + FilenameStriper.getIDAsStripedPath(assessment.getSpeciesID()) + "_/" + assessment.getAssessmentID() + ".xml";
	}
	
	public static String getPathForDraftAssessment(String assessmentID) {
		return getDraftAssessmentUrl() + FilenameStriper.getIDAsStripedPath(assessmentID.substring(
				0, assessmentID.indexOf("_")-1)) + "_/" + assessmentID + ".xml";
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
}
