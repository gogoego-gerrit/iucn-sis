package org.iucn.sis.shared.api.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class TaxomaticOperation implements Serializable, Comparable<TaxomaticOperation> {
	
	private int id;
	
	private User user;
	
	private java.util.Date date;
	
	private String operation;
	
	private String instructions;
	
	private Set<TaxomaticHistory> history;
	
	private String details;
	
	public TaxomaticOperation() {
		history = new HashSet<TaxomaticHistory>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public java.util.Date getDate() {
		return date;
	}

	public void setDate(java.util.Date date) {
		this.date = date;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	
	public Set<TaxomaticHistory> getHistory() {
		return history;
	}
	
	public void setHistory(Set<TaxomaticHistory> history) {
		this.history = history;
	}
	
	public String getDetails() {
		return details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	public String toXML() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<operation id=\"" + getId() + "\" type=\"" + getOperation() + "\">");
		builder.append(user.toBasicXML());
		builder.append(XMLWritingUtils.writeTag("date", getDate().getTime() + ""));
		builder.append(XMLWritingUtils.writeCDATATag("instructions", getInstructions()));
		builder.append(XMLWritingUtils.writeCDATATag("details", details, true));
		
		for (TaxomaticHistory current : getHistory())
			builder.append(current.getTaxon().toXMLMinimal());
		
		builder.append("</operation>");
		return builder.toString();
	}
	
	public static TaxomaticOperation fromXML(NativeElement root) {
		TaxomaticOperation operation = new TaxomaticOperation();
		operation.setId(Integer.valueOf(root.getAttribute("id")));
		operation.setOperation(root.getAttribute("type"));
		
		NativeNodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if (User.ROOT_TAG.equals(node.getNodeName()))
				operation.setUser(User.fromXML((NativeElement)node));
			else if ("date".equals(node.getNodeName()))
				operation.setDate(new java.util.Date(Long.parseLong(node.getTextContent())));
			else if ("instructions".equals(node.getNodeName()))
				operation.setInstructions(node.getTextContent());
			else if ("details".equals(node.getNodeName()))
				operation.setDetails(node.getTextContent());
			else if (Taxon.ROOT_TAG.equals(node.getNodeName())) {
				TaxomaticHistory history = new TaxomaticHistory();
				history.setTaxon(Taxon.fromXMLminimal((NativeElement)node));
				
				operation.getHistory().add(history);
			}
		}
		
		return operation;
	}
	
	@Override
	public int compareTo(TaxomaticOperation o) {
		if (this.getId() == o.getId())
			return 0;
		
		return getDate().compareTo(o.getDate());
	}

}
