package org.iucn.sis.client.data.assessments;

import java.util.ArrayList;
import java.util.List;

public class AssessmentFetchRequest {

	private List<String> taxonIDs;
	private List<String> assessmentUIDs;
	
	public AssessmentFetchRequest() {
		assessmentUIDs = new ArrayList<String>();
		taxonIDs = new ArrayList<String>();
	}
	
	/**
	 * This constructor accepts an Assessment UID, which is added to the list of
	 * assessments for you.
	 * 
	 * @param assessmentUID - the assessment's UID - see AssessmentData.getUID()
	 */
	public AssessmentFetchRequest(String assessmentUID) {
		this(assessmentUID, null);
	}
	
	/**
	 * This constructor accepts an Assessment UID and a taxon ID, which is added to 
	 * appropriate lists for you. Parameters can be null.
	 * 
	 * @param assessmentUID - the assessment's UID - see AssessmentData.getUID()
	 * @param taxonID - a taxon ID, will fetch all related assessments
	 */
	public AssessmentFetchRequest(String assessmentUID, String taxonID) {
		assessmentUIDs = new ArrayList<String>();
		taxonIDs = new ArrayList<String>();
		
		if( assessmentUID != null )
			assessmentUIDs.add(assessmentUID);
		if( taxonID != null )
			taxonIDs.add(taxonID);
	}
	
	/**
	 * This constructor accepts an Assessment UID and a taxon ID, which is added to 
	 * appropriate lists for you. Parameters can be null.
	 * 
	 * @param assessmentUID - the assessment's UID - see AssessmentData.getUID()
	 * @param taxonID - a taxon ID, will fetch all related assessments
	 */
	public AssessmentFetchRequest(List<String> assessmentUID, List<String> taxonID) {
		assessmentUIDs = new ArrayList<String>();
		taxonIDs = new ArrayList<String>();
		
		if( assessmentUID != null )
			assessmentUIDs.addAll(assessmentUID);
		if( taxonID != null )
			taxonIDs.addAll(taxonID);
	}
	
	public void addForTaxon(String taxonID) {
		taxonIDs.add(taxonID);
	}
	
	public void addForTaxa(List<String> taxaIDs) {
		taxonIDs.addAll(taxaIDs);
	}
	
	public void addAssessment(String assessmentUID) {
		assessmentUIDs.add(assessmentUID);
	}
	
	public void addAssessments(List<String> assessmentUIDs) {
		assessmentUIDs.addAll(assessmentUIDs);
	}
	
	public List<String> getAssessmentUIDs() {
		return assessmentUIDs;
	}
	
	public List<String> getTaxonIDs() {
		return taxonIDs;
	}
	
	public String toXML() {
		StringBuilder ret = new StringBuilder("<fetch>");
		
		for( String uid : assessmentUIDs ) {
			ret.append("<uid>");
			ret.append(uid);
			ret.append("</uid>");
		}
		
		for( String taxonID : taxonIDs ) {
			ret.append("<taxon>");
			ret.append(taxonID);
			ret.append("</taxon>");
		}
		
		ret.append("</fetch>");
		return ret.toString();
	}
}
