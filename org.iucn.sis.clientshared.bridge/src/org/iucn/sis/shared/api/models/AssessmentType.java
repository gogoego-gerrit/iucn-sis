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
	
	public static final int DRAFT_ASSESSMENT_STATUS_ID = 2;
	public static final int PUBLISHED_ASSESSMENT_STATUS_ID = 1;
	public static final int USER_ASSESSMENT_STATUS_ID = 3 ;
	
	public static final String DRAFT_ASSESSMENT_TYPE = "draft_status";
	public static final String PUBLISHED_ASSESSMENT_TYPE = "published_status";
	public static final String USER_ASSESSMENT_TYPE = "user_status";
	
	public static AssessmentType getAssessmentType(String type) {
		AssessmentType assType = new AssessmentType();
		if (type.equalsIgnoreCase(DRAFT_ASSESSMENT_TYPE)) {
			assType.setId(DRAFT_ASSESSMENT_STATUS_ID);
			assType.setName(DRAFT_ASSESSMENT_TYPE);
		} else if (type.equalsIgnoreCase(PUBLISHED_ASSESSMENT_TYPE)) {
			assType.setId(PUBLISHED_ASSESSMENT_STATUS_ID);
			assType.setName(PUBLISHED_ASSESSMENT_TYPE);
		} else if (type.equalsIgnoreCase(USER_ASSESSMENT_TYPE)) {
			assType.setId(USER_ASSESSMENT_STATUS_ID);
			assType.setName(USER_ASSESSMENT_TYPE);
		} else {
			return null;
		}
		return assType;
			
	}
	
	public static AssessmentType getAssessmentType(int id) {
		AssessmentType assType = new AssessmentType();
		if (id == DRAFT_ASSESSMENT_STATUS_ID) {
			assType.setId(DRAFT_ASSESSMENT_STATUS_ID);
			assType.setName(DRAFT_ASSESSMENT_TYPE);
		} else if (id == PUBLISHED_ASSESSMENT_STATUS_ID) {
			assType.setId(PUBLISHED_ASSESSMENT_STATUS_ID);
			assType.setName(PUBLISHED_ASSESSMENT_TYPE);
		} else if (id == USER_ASSESSMENT_STATUS_ID) {
			assType.setId(USER_ASSESSMENT_STATUS_ID);
			assType.setName(USER_ASSESSMENT_TYPE);
		} else {
			return null;
		}
		return assType;
			
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
		return getName().replace("_status", "");
	}
	
}
