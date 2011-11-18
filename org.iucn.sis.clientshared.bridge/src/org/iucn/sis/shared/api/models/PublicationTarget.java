package org.iucn.sis.shared.api.models;

import java.io.Serializable;
import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class PublicationTarget implements Serializable, Comparable<PublicationTarget> {
	
	public static final String ROOT_TAG = "publicationTarget";
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private String name;
	
	private java.util.Date date;
	
	private Reference reference;
	
	public PublicationTarget() {
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public java.util.Date getDate() {
		return date;
	}
	
	public void setDate(java.util.Date date) {
		this.date = date;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public void setReference(Reference reference) {
		this.reference = reference;
	}
	
	public String toXML() {
		return toXML(ROOT_TAG);
	}
	
	public String toXML(String ROOT_TAG) {
		StringBuilder out = new StringBuilder();
		out.append("<" + ROOT_TAG + " id=\"" + getId() + "\">");
		out.append(XMLWritingUtils.writeCDATATag("name", getName()));
		if (getDate() != null)
			out.append(XMLWritingUtils.writeTag("date", Long.toString(getDate().getTime())));
		if (reference != null)
			out.append(reference.toXML());
		out.append("</"+ROOT_TAG+">");
		
		return out.toString();
	}
	
	@Override
	public int compareTo(PublicationTarget o) {
		return name.compareTo(o.name);
	}
	
	public static PublicationTarget fromXML(NativeElement element) {
		PublicationTarget target = new PublicationTarget();
		target.setId(Integer.valueOf(element.getAttribute("id")));
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode current = nodes.item(i);
			if ("name".equals(current.getNodeName()))
				target.setName(current.getTextContent());
			else if ("date".equals(current.getNodeName()))
				target.setDate(new Date(Long.valueOf(current.getTextContent())));
			else if (Reference.ROOT_TAG.equals(current.getNodeName()))
				target.setReference(Reference.fromXML(current));
		}
		
		return target;
	}

}
