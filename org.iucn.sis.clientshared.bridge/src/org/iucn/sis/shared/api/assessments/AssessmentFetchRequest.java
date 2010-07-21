package org.iucn.sis.shared.api.assessments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class AssessmentFetchRequest {

	public static final String ROOT_TAG = "fetch";
	
	private Set<Integer> taxonIDs;
	private Set<Integer> assessmentIDs;
	
	public AssessmentFetchRequest() {
		assessmentIDs = new HashSet<Integer>();
		taxonIDs = new HashSet<Integer>();
	}
	
	/**
	 * This constructor accepts an Assessment UID, which is added to the list of
	 * assessments for you.
	 * 
	 * @param assessmentUID - the assessment's UID - see AssessmentData.getUID()
	 */
	public AssessmentFetchRequest(Integer assessmentUID) {
		this(assessmentUID, null);
	}
	
	/**
	 * This constructor accepts an Assessment UID and a taxon ID, which is added to 
	 * appropriate lists for you. Parameters can be null.
	 * 
	 * @param assessmentUID - the assessment's UID - see AssessmentData.getUID()
	 * @param taxonID - a taxon ID, will fetch all related assessments
	 */
	public AssessmentFetchRequest(Integer assessmentUID, Integer taxonID) {
		assessmentIDs = new HashSet<Integer>();
		taxonIDs = new HashSet<Integer>();
		
		if( assessmentUID != null )
			assessmentIDs.add(assessmentUID);
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
	public AssessmentFetchRequest(List<Integer> assessmentUID, List<Integer> taxonID) {
		assessmentIDs = new HashSet<Integer>();
		taxonIDs = new HashSet<Integer>();
		
		if( assessmentUID != null )
			assessmentIDs.addAll(assessmentUID);
		if( taxonID != null )
			taxonIDs.addAll(taxonID);
	}
	
	public void addForTaxon(Integer taxonID) {
		taxonIDs.add(taxonID);
	}
	
	public void addForTaxa(List<Integer> taxaIDs) {
		taxonIDs.addAll(taxaIDs);
	}
	
	public void addAssessment(Integer assessmentUID) {
		assessmentIDs.add(assessmentUID);
	}
	
	public void addAssessments(List<Integer> assessmentUIDs) {
		assessmentUIDs.addAll(assessmentUIDs);
	}
	
	public Set<Integer> getAssessmentUIDs() {
		return assessmentIDs;
	}
	
	public Set<Integer> getTaxonIDs() {
		return taxonIDs;
	}
	
	public String toXML() {
		StringBuilder ret = new StringBuilder("<" + ROOT_TAG + ">");
		
		for( Integer uid : assessmentIDs ) {
			ret.append("<assessmentID>");
			ret.append(uid);
			ret.append("</assessmentID>");
		}
		
		for( Integer taxonID : taxonIDs ) {
			ret.append("<taxonID>");
			ret.append(taxonID);
			ret.append("</taxonID>");
		}
		
		ret.append("</" + ROOT_TAG + ">");
		return ret.toString();
	}
	
	public static AssessmentFetchRequest fromXML(NativeElement element) {
		AssessmentFetchRequest request = new AssessmentFetchRequest();
		NativeNodeList assms = element.getElementsByTagName("assessmentID");
		for (int i = 0; i < assms.getLength(); i++) {
			request.getAssessmentUIDs().add(Integer.valueOf(assms.elementAt(i).getTextContent()));
		}
		NativeNodeList taxons = element.getElementsByTagName("taxonID");
		for (int i = 0; i < taxons.getLength(); i++) {
			request.getTaxonIDs().add(Integer.valueOf(taxons.elementAt(i).getTextContent()));
		}
		return request;
	}
}
