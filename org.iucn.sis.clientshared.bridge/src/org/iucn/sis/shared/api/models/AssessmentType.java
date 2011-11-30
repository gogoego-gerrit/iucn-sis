package org.iucn.sis.shared.api.models;
/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import java.io.Serializable;

import com.solertium.lwxml.shared.NativeElement;
public class AssessmentType implements Serializable {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public static final String ROOT_TAG = "assessment_type";
	
	public static final int PUBLISHED_ASSESSMENT_STATUS_ID = 1;
	public static final int DRAFT_ASSESSMENT_STATUS_ID = 2;
	public static final int SUBMITTED_ASSESSMENT_STATUS_ID = 3;
	public static final int FOR_PUBLICATION_ASSESSMENT_STATUS_ID = 4;
	
	public static final String PUBLISHED_ASSESSMENT_TYPE = "published_status";
	public static final String DRAFT_ASSESSMENT_TYPE = "draft_status";
	public static final String SUBMITTED_ASSESSMENT_TYPE = "submitted_status";
	public static final String FOR_PUBLICATION_ASSESSMENT_TYPE = "for_publication_status";
	
	public static AssessmentType getAssessmentType(String key) {
		AssessmentType type = new AssessmentType();
		if (key.equalsIgnoreCase(DRAFT_ASSESSMENT_TYPE)) {
			type.setId(DRAFT_ASSESSMENT_STATUS_ID);
			type.setName(DRAFT_ASSESSMENT_TYPE);
		} else if (key.equalsIgnoreCase(PUBLISHED_ASSESSMENT_TYPE)) {
			type.setId(PUBLISHED_ASSESSMENT_STATUS_ID);
			type.setName(PUBLISHED_ASSESSMENT_TYPE);
		} else if (key.equalsIgnoreCase(SUBMITTED_ASSESSMENT_TYPE)) {
			type.setId(SUBMITTED_ASSESSMENT_STATUS_ID);
			type.setName(SUBMITTED_ASSESSMENT_TYPE);
		} else if (key.equalsIgnoreCase(FOR_PUBLICATION_ASSESSMENT_TYPE)) {
			type.setId(FOR_PUBLICATION_ASSESSMENT_STATUS_ID);
			type.setName(FOR_PUBLICATION_ASSESSMENT_TYPE);
		} else {
			return null;
		}
		
		return type;
	}
	
	public static AssessmentType getAssessmentType(int id) {
		AssessmentType type = new AssessmentType();
		if (id == DRAFT_ASSESSMENT_STATUS_ID) {
			type.setId(DRAFT_ASSESSMENT_STATUS_ID);
			type.setName(DRAFT_ASSESSMENT_TYPE);
		} else if (id == PUBLISHED_ASSESSMENT_STATUS_ID) {
			type.setId(PUBLISHED_ASSESSMENT_STATUS_ID);
			type.setName(PUBLISHED_ASSESSMENT_TYPE);
		} else if (id == SUBMITTED_ASSESSMENT_STATUS_ID) {
			type.setId(SUBMITTED_ASSESSMENT_STATUS_ID);
			type.setName(SUBMITTED_ASSESSMENT_TYPE);
		} else if (id == FOR_PUBLICATION_ASSESSMENT_STATUS_ID) {
			type.setId(FOR_PUBLICATION_ASSESSMENT_STATUS_ID);
			type.setName(FOR_PUBLICATION_ASSESSMENT_TYPE);
		} else {
			return null;
		}
		
		return type;
	}
	
	public static AssessmentType fromXML(NativeElement element) {
		return getAssessmentType(Integer.valueOf(element.getAttribute("id")));
	}
	
	public String toXML() {
		return "<" + AssessmentType.ROOT_TAG + " id=\"" + getId() + "\"/>";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AssessmentType) {
			return ((AssessmentType)obj).getId() == getId();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toXML().hashCode();
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	
	
	public AssessmentType() {
	}
	
	private int id;
	
	private String name;
	
	private java.util.Set<WorkingSet> workingSet = new java.util.HashSet<WorkingSet>();
	
	private java.util.Set<Assessment> assessment = new java.util.HashSet<Assessment>();
	
	private void setId(int value) {
		this.id = value;
	}
	
	public int getId() {
		return id;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setWorkingSet(java.util.Set<WorkingSet> value) {
		this.workingSet = value;
	}
	
	public java.util.Set<WorkingSet> getWorkingSet() {
		return workingSet;
	}
	
	
	public void setAssessment(java.util.Set<Assessment> value) {
		this.assessment = value;
	}
	
	public java.util.Set<Assessment> getAssessment() {
		return assessment;
	}
	
	
	public String toString() {
		return String.valueOf(getId());
	}
	
	public String getDisplayName() {
		return getDisplayName(false);
	}
	
	public String getDisplayName(boolean capitalize) {
		if (FOR_PUBLICATION_ASSESSMENT_TYPE.equals(getName()))
			return capitalize ? "For Publication" : "for publication";
		
		String name = getName().replace("_status", "");
		if (capitalize)
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		return name;
	}
	
}
