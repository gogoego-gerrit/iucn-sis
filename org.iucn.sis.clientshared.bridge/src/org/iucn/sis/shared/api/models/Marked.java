package org.iucn.sis.shared.api.models;

import java.io.Serializable;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class Marked implements Serializable {
	
	private int id;
	
	private String type;
	
	private int objectid;
	
	private String mark;
	
	private User user;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getObjectid() {
		return objectid;
	}

	public void setObjectid(int objectid) {
		this.objectid = objectid;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<marked id=\"" + getId() + "\">");
		xml.append(XMLWritingUtils.writeTag("type", type));
		xml.append(XMLWritingUtils.writeTag("objectid", objectid+""));
		xml.append(XMLWritingUtils.writeTag("mark", mark));
		xml.append(user.toXML());
		xml.append("</marked>");
		return xml.toString();
	}
	
	public static Marked fromXML(NativeElement element) {
		Marked marked = new Marked();
		marked.setId(Integer.valueOf(element.getAttribute("id")));
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i); 
			if ("type".equals(node.getNodeName()))
				marked.setType(node.getTextContent());
			else if ("objectid".equals(node.getNodeName()))
				marked.setObjectid(Integer.valueOf(node.getTextContent()));
			else if ("mark".equals(node.getNodeName()))
				marked.setMark(node.getTextContent());
			else if ("user".equals(node.getNodeName()))
				marked.setUser(User.fromXML((NativeElement)node));
		}
		
		return marked;
	}

}
