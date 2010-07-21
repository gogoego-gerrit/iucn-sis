package org.iucn.sis.shared.api.assessments;

import com.solertium.lwxml.shared.NativeElement;

public class AssessmentAttachment {
	
	public String filename;
	public boolean isPublished;
	public String assessmentID;
	public String id;
	
	public AssessmentAttachment() {}
	
	public AssessmentAttachment(NativeElement element) {
		this.assessmentID = element.getAttribute("assessmentID");
		this.id = element.getAttribute("id");
		this.filename = element.getElementsByTagName("filename").elementAt(0).getTextContent();
		this.isPublished = Boolean.parseBoolean(element.getElementsByTagName("published").elementAt(0).getTextContent());
	}
	
	public String toXML() {
		return "<attachment assessmentID=\"" + this.assessmentID + "\" id=\"" + this.id + "\">\r\n<filename>" + filename + 
		"</filename>\r\n<published>" + isPublished + "</published>\r\n</attachment>";  
	}
	
}
