package org.iucn.sis.shared.api.models;

import java.io.Serializable;
import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class PublicationData implements Serializable {
	
	public static final String ROOT_TAG = "publication";
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private Assessment assessment;
	
	public String group;
	
	public java.util.Date submissionDate;
	
	public User submitter;
	
	public PublicationTarget targetGoal;
	
	public PublicationTarget targetApproved;
	
	public String notes;
	
	public PublicationData() {
	}
	
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public void setSubmissionDate(java.util.Date submissionDate) {
		this.submissionDate = submissionDate;
	}
	
	public java.util.Date getSubmissionDate() {
		return submissionDate;
	}
	
	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}
	
	public User getSubmitter() {
		return submitter;
	}
	
	public void setTargetApproved(PublicationTarget targetApproved) {
		this.targetApproved = targetApproved;
	}
	
	public PublicationTarget getTargetApproved() {
		return targetApproved;
	}
	
	public void setTargetGoal(PublicationTarget targetGoal) {
		this.targetGoal = targetGoal;
	}
	
	public PublicationTarget getTargetGoal() {
		return targetGoal;
	}
	
	public String toXML() {
		StringBuilder out = new StringBuilder();
		out.append("<"+ROOT_TAG+" id=\"" + getId() + "\">");
		out.append(toXML(getAssessment()));
		out.append(XMLWritingUtils.writeCDATATag("group", getGroup(), true));
		out.append(XMLWritingUtils.writeCDATATag("notes", getNotes(), true));
		
		if (getSubmissionDate() != null)
			out.append(XMLWritingUtils.writeTag("date", Long.toString(getSubmissionDate().getTime())));
		
		if (getSubmitter() != null)
			out.append(getSubmitter().toBasicXML());
		
		if (getTargetGoal() != null)
			out.append(getTargetGoal().toXML("targetGoal"));
		
		if (getTargetApproved() != null)
			out.append(getTargetApproved().toXML("targetApproved"));

		out.append("</" + ROOT_TAG + ">");
		return out.toString();
	}
	
	private String toXML(Assessment assessment) {
		StringBuilder out = new StringBuilder();
		out.append("<assessment id=\"" + getId() + "\">");
		out.append(assessment.getTaxon().toXMLMinimal());
		out.append(assessment.getAssessmentType().toXML());
		out.append("</assessment>");
		return out.toString();
	}
	
	public void setAssessmentId(int id) {
		Assessment assessment = new Assessment();
		assessment.setId(id);
		
		setAssessment(assessment);
	}
	
	private static Assessment assessmentFromXML(NativeElement element) {
		Assessment assessment = new Assessment();
		assessment.setId(Integer.valueOf(element.getAttribute("id")));
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if (Taxon.ROOT_TAG.equals(node.getNodeName()))
				assessment.setTaxon(Taxon.fromXMLminimal(((NativeElement)node)));
			else if (AssessmentType.ROOT_TAG.equals(node.getNodeName()))
				assessment.setAssessmentType(AssessmentType.fromXML(((NativeElement)node)));
		}
		
		return assessment;
	}
	
	public static PublicationData fromXML(NativeElement element) {
		PublicationData data = new PublicationData();
		data.setId(Integer.valueOf(element.getAttribute("id")));
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if ("assessment".equals(node.getNodeName()))
				data.setAssessment(assessmentFromXML((NativeElement)node));
			else if ("group".equals(node.getNodeName()))
				data.setGroup(node.getTextContent());
			else if ("notes".equals(node.getNodeName()))
				data.setNotes(node.getTextContent());
			else if ("date".equals(node.getNodeName()))
				data.setSubmissionDate(new Date(Long.valueOf(node.getTextContent())));
			else if (User.ROOT_TAG.equals(node.getNodeName()))
				data.setSubmitter(User.fromXML((NativeElement)node));
			else if ("targetGoal".equals(node.getNodeName()))
				data.setTargetGoal(PublicationTarget.fromXML((NativeElement)node));
			else if ("targetApproved".equals(node.getNodeName()))
				data.setTargetApproved(PublicationTarget.fromXML((NativeElement)node));
		}
		
		return data;
	}

}
