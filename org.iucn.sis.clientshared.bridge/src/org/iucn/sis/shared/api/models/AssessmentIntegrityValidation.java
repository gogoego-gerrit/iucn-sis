package org.iucn.sis.shared.api.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class AssessmentIntegrityValidation implements Serializable {
	
	public static final int SUCCESS = 1;
	public static final int WARNING = 2;
	public static final int FAILURE = 3;
	
	public static final String ROOT_TAG = "validation";
	
	private int id;
	
	private Assessment assessment;
	
	private String rule;
	
	private int status;
	
	private String message;
	
	private java.util.Date date;
	
	private Collection<String> messages;
	
	public AssessmentIntegrityValidation() {
		date = new Date();
		status = SUCCESS;
	}
	
	public AssessmentIntegrityValidation(Collection<String> errors) {
		this();
		status = FAILURE;
		messages = new ArrayList<String>(errors);
		
		StringBuilder message = new StringBuilder();
		for (final Iterator<String> iter = errors.iterator(); iter.hasNext(); ) {
			message.append(iter.next());
			if (iter.hasNext())
				message.append('|');
		}
		
		this.message = message.toString();
	}
	
	public int getId() {
		return id;
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	
	public String getRule() {
		return rule;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public java.util.Date getDate() {
		return date;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	public void setMessage(String message) {
		this.message = message;
		this.messages = null;
	}
	
	public void setDate(java.util.Date date) {
		this.date = date;
	}
	
	public Collection<String> getMessages() {
		if (messages == null && message != null) {
			messages = new ArrayList<String>();
			StringBuilder builder = new StringBuilder();
			for (char c : message.toCharArray()) {
				if (c != '|')
					builder.append(c);
				else {
					messages.add(builder.toString());
					builder = new StringBuilder();
				}
			}
			String finalMsg = builder.toString();
			if (finalMsg.length() > 0)
				messages.add(builder.toString());
		}
		return messages;
	}
	
	public boolean isSuccess() {
		return status == SUCCESS;
	}
	
	public boolean isFailure() {
		return status == FAILURE;
	}
	
	public boolean hasWarnings() {
		return status == WARNING;
	}
	
	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<" + ROOT_TAG + " id=\"" + getId() + "\">");
		xml.append(XMLWritingUtils.writeCDATATag("rule", getRule()));
		xml.append(XMLWritingUtils.writeCDATATag("status", Integer.toString(getStatus())));
		xml.append(XMLWritingUtils.writeCDATATag("message", getMessage()));
		xml.append(XMLWritingUtils.writeCDATATag("date", getDate().getTime()+""));
		xml.append("</" + ROOT_TAG + ">");
		
		return xml.toString();
	}
	
	public static AssessmentIntegrityValidation fromXML(NativeElement root) {
		AssessmentIntegrityValidation validation = new AssessmentIntegrityValidation();
		validation.setId(Integer.valueOf(root.getAttribute("id")));
		
		NativeNodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode current = nodes.item(i);
			if ("rule".equals(current.getNodeName()))
				validation.setRule(current.getTextContent());
			else if ("status".equals(current.getNodeName()))
				validation.setStatus(Integer.valueOf(current.getTextContent()));
			else if ("message".equals(current.getNodeName()))
				validation.setMessage(current.getTextContent());
			else if ("date".equals(current.getNodeName()))
				validation.setDate(new Date(Long.valueOf(current.getTextContent())));
		}
		
		return validation;
	}

}
