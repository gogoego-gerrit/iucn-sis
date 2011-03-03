package org.iucn.sis.shared.api.models;

import java.io.Serializable;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class AssessmentChange implements Serializable {
	
	public static final int ADD = 0;
	public static final int EDIT = 1;
	public static final int DELETE = 2;
	
	private int id;
	
	private Assessment assessment;
	
	private String fieldName;
	
	private Edit edit;
	
	private Field oldField;
	
	private Field newField;
	
	private int type;
	
	public AssessmentChange() {
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Edit getEdit() {
		return edit;
	}

	public void setEdit(Edit edit) {
		this.edit = edit;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Field getOldField() {
		return oldField;
	}

	public void setOldField(Field oldField) {
		this.oldField = oldField;
	}

	public Field getNewField() {
		return newField;
	}

	public void setNewField(Field newField) {
		this.newField = newField;
	}
	
	public int getType() {
		return type;
	}
	
	/*
	 * Use the static int values of 
	 * ADD, EDIT, or DELETE to specify 
	 * a change type.
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<change id=\"" + id + "\" type=\"" + type + "\">");
		xml.append(XMLWritingUtils.writeCDATATag("fieldName", getFieldName()));
		if (assessment != null)
			xml.append(XMLWritingUtils.writeTag("assessment", getAssessment().getId()+""));
		if (edit != null)
			xml.append(edit.toXML());
		if (oldField != null)
			xml.append(oldField.toXML("oldField"));
		if (newField != null)
			xml.append(newField.toXML("newField"));
		xml.append("</change>");
		
		return xml.toString();
	}
	
	public static AssessmentChange fromXML(NativeElement element) {
		AssessmentChange assessmentChange = new AssessmentChange();
		assessmentChange.setId(Integer.valueOf(element.getAttribute("id")));
		assessmentChange.setType(Integer.valueOf(element.getAttribute("type")));
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if ("fieldName".equals(node.getNodeName()))
				assessmentChange.setFieldName(node.getTextContent());
			else if (Edit.ROOT_TAG.equals(node.getNodeName()))
				assessmentChange.setEdit(Edit.fromXML((NativeElement)node));
			else if ("oldField".equals(node.getNodeName()))
				assessmentChange.setOldField(Field.fromXML((NativeElement)node));
			else if ("newField".equals(node.getNodeName()))
				assessmentChange.setNewField(Field.fromXML((NativeElement)node));
		}
		
		return assessmentChange;
	}

}
