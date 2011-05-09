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
import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;
public class Edit implements Serializable, Comparable<Edit> {
	
	public static final String ROOT_TAG = "edit";
	
	public static Edit fromXML(NativeElement element) {
		Edit edit = new Edit();
		edit.setId(Integer.parseInt(element.getAttribute("id")));
		edit.setUser(User.fromXML(element.getElementsByTagName(User.ROOT_TAG).elementAt(0)));
		edit.setCreatedDate(new Date(Long.parseLong(element.getElementsByTagName("date").elementAt(0).getTextContent())));
		return edit;
	}
	
	private int id;
	
	private Date createdDate;
	
	private User user;
	
	private java.util.Set<WorkingSet> working_set;
	
	private java.util.Set<Assessment> assessment;
	
	private java.util.Set<Taxon> taxon;
	
	private java.util.Set<Notes> notes;
	
	private java.util.Set<FieldAttachment> attachments;
	
	public Edit() {
		createdDate = new Date();
		working_set = new java.util.HashSet<WorkingSet>();
		assessment = new java.util.HashSet<Assessment>();
		taxon = new java.util.HashSet<Taxon>();
		notes = new java.util.HashSet<Notes>();
		attachments = new java.util.HashSet<FieldAttachment>();
	}
	
	@Override
	public int compareTo(Edit o) {
		if (this == o)
			return 0;
			
		Date d = new Date(getCreatedDate().getTime());
		Date od = new Date(o.getCreatedDate().getTime());
		
		return d.compareTo(od);
	}
	
	public java.util.Set<Assessment> getAssessment() {
		return assessment;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}
	
	public int getId() {
		return id;
	}
	
	public java.util.Set<Notes> getNotes() {
		return notes;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public java.util.Set<Taxon> getTaxon() {
		return taxon;
	}
	
	public User getUser() {
		return user;
	}
	
	public java.util.Set<WorkingSet> getWorking_set() {
		return working_set;
	}
	
	
	public void setAssessment(java.util.Set<Assessment> value) {
		this.assessment = value;
	}
	
	public java.util.Set<FieldAttachment> getAttachments() {
		return attachments;
	}
	
	public void setAttachments(java.util.Set<FieldAttachment> attachments) {
		this.attachments = attachments;
	}
	
	public void setCreatedDate(Date value) {
		this.createdDate = value;
	}
	
	
	private void setId(int value) {
		this.id = value;
	}
	
	public void setNotes(java.util.Set<Notes> value) {
		this.notes = value;
	}
	
	
	public void setTaxon(java.util.Set<Taxon> value) {
		this.taxon = value;
	}
	
	public void setUser(User value) {
		this.user = value;
	}
	
	
	public void setWorking_set(java.util.Set<WorkingSet> value) {
		this.working_set = value;
	}
	
	public String toString() {
		return "EDIT " + String.valueOf(getId());
	}
	
	public String toXML() {
		return "<" + ROOT_TAG + " id=\"" + getId() + "\" >" + getUser().toBasicXML()  + "<date>" + getCreatedDate().getTime() + "</date></" + ROOT_TAG + ">";
	}
	
}
