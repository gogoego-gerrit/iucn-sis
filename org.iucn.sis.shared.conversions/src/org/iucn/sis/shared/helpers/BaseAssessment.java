package org.iucn.sis.shared.helpers;

import java.io.Serializable;

public class BaseAssessment implements Serializable {
	public static final String USER_ASSESSMENT_STATUS = "user_status";
	public static final String DRAFT_ASSESSMENT_STATUS = "draft_status";
	public static final String PUBLISHED_ASSESSMENT_STATUS = "published_status";

	public static final int ASSESSMENT_IN_PROGRESS = 0;
	public static final int ASSESSMENT_IN_REVIEW = 1;
	public static final int ASSESSMENT_REVIEW_COMPLETE = 2;
	public static final int ASSESSMENT_IN_RL_EVALUATION = 3;
	public static final int ASSESSMENT_EVALUATION_COMPLETE = 4;
	public static final int ASSESSMENT_SUBMIT_FOR_PUB = 5;
	public static final int ASSESSMENT_ACCEPTED_FOR_PUB = 6;
	public static final int ASSESSMENT_PUBLISHED = 7;
	public static final int ASSESSMENT_IN_PETITION = 8;

	/**
	 * For transitioning period, this represents the "region" ID that == global.
	 */
	public static final String GLOBAL_ID = "-1"; 
	
	private static final long serialVersionUID = 6376527423219050086L;

	// METADATA
	protected String assessmentID = "";

	protected String speciesID = "";
	protected String speciesName = "";

	protected boolean isDone = true;
	protected boolean isLocked = false;
	protected boolean isHistorical = false;

	protected String type = "";

	protected String dateAdded = "";
	protected long dateModified = 0;
	protected String dateFinalized = "";

	protected String source = "";
	protected String sourceDate = "";

	protected String categoryFuzzyResult = "";
	protected String categoryText = "";

	protected String crCriteria = "";
	protected String enCriteria = "";
	protected String vuCriteria = "";
	
	protected String userLastUpdated = null;
	

	public BaseAssessment() {

	}

	/**
	 * Copies the BaseAssessment, saving it as a User assessment, meaning it
	 * gets an assessmentID == speciesID
	 * 
	 * @param copy
	 */
	public BaseAssessment(BaseAssessment copy, String validationStatus) {
		assessmentID = copy.speciesID;
		speciesID = copy.speciesID;
		speciesName = copy.speciesName;

		this.type = validationStatus;
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public String getCrCriteria() {
		return crCriteria;
	}
	
	public String getEnCriteria() {
		return enCriteria;
	}
	
	public String getVuCriteria() {
		return vuCriteria;
	}
	
	public void setCrCriteria(String crCriteria) {
		this.crCriteria = crCriteria;
	}
	
	public void setEnCriteria(String enCriteria) {
		this.enCriteria = enCriteria;
	}
	
	public void setVuCriteria(String vuCriteria) {
		this.vuCriteria = vuCriteria;
	}
	
	public String getDateAdded() {
		return dateAdded;
	}

	public String getDateFinalized() {
		return dateFinalized;
	}

	public long getDateModified() {
		return dateModified;
	}

	public String getSource() {
		return source;
	}

	public String getSourceDate() {
		return sourceDate;
	}

	public String getSpeciesID() {
		return speciesID;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public String getType() {
		return type;
	}
	
	public String getUID() {
		return assessmentID + "_" + type;
	}
	
	public String getUserLastUpdated() {
		return userLastUpdated;
	}

	public boolean isDone() {
		return isDone;
	}

	public boolean isHistorical() {
		return isHistorical;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public void setDateAdded(String dateAdded) {
		this.dateAdded = dateAdded;
	}

	public void setDateFinalized(String dateFinalized) {
		this.dateFinalized = dateFinalized;
	}

	public void setDateModified(long dateModified) {
		this.dateModified = dateModified;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public void setHistorical(boolean isHistorical) {
		this.isHistorical = isHistorical;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setSourceDate(String sourceDate) {
		this.sourceDate = sourceDate;
	}

	public void setSpeciesID(String speciesID) {
		this.speciesID = speciesID;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}

	public void setType(String validationStatus) {
		this.type = validationStatus;
	}
	
	public void setUserLastUpdated(String userLastUpdated) {
		this.userLastUpdated = userLastUpdated;
	}

	public String toXML() {
		try {
			String xml = "";

			xml += "<assessment id=\"" + assessmentID + "\" xsi:noNamespaceSchemaLocation"
					+ "=\"assessmentStructure.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\r\n\r\n";

			xml += "<basicInformation>\r\n";

			xml += "<assessmentID>" + assessmentID + "</assessmentID>\r\n";
			xml += "<speciesID>" + speciesID + "</speciesID>\r\n";
			xml += "<speciesName>" + speciesName + "</speciesName>\r\n";
			xml += "<source>" + XMLUtils.clean(source) + "</source>\r\n";
			xml += "<sourceDate>" + sourceDate + "</sourceDate>\r\n";

			xml += "<dateModified>" + dateModified + "</dateModified>\r\n";
			xml += "<dateAdded>" + dateAdded + "</dateAdded>\r\n";
			xml += "<dateFinalized>" + dateFinalized + "</dateFinalized>\r\n";

			xml += "<validationStatus>" + type + "</validationStatus>\r\n";
			xml += "<isDone>" + isDone + "</isDone>";

			xml += "<isHistorical>" + isHistorical + "</isHistorical>";

			xml += "<crCriteria>" + crCriteria + "</crCriteria>";
			xml += "<enCriteria>" + enCriteria + "</enCriteria>";
			xml += "<vuCriteria>" + vuCriteria + "</vuCriteria>";
			xml += "<lastUpdatedBy>" + XMLUtils.clean(userLastUpdated) + "</lastUpdatedBy>";
			
			xml += "</basicInformation>\r\n";

			return xml;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}

}