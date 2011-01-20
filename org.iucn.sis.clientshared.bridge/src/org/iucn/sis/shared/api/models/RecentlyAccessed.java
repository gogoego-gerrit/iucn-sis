package org.iucn.sis.shared.api.models;

import java.io.Serializable;
import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class RecentlyAccessed implements Serializable {
	
	public static final String ASSESSMENT = "assessment";
	public static final String USER = "user";
	
	private int id;
	
	private String type;
	
	private int objectid;
	
	private java.util.Date date;
	
	private User user;
	
	public RecentlyAccessed() {
	}
	
	public java.util.Date getDate() {
		return date;
	}
	
	public int getId() {
		return id;
	}
	
	public int getObjectid() {
		return objectid;
	}
	
	public String getType() {
		return type;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setObjectid(int objectid) {
		this.objectid = objectid;
	}
	
	public void setDate(java.util.Date date) {
		this.date = date;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public String toXML() {
		StringBuilder out = new StringBuilder();
		out.append("<recent id=\"" + getId() + "\">");
		out.append(XMLWritingUtils.writeTag("type", getType()));
		out.append(XMLWritingUtils.writeTag("objectid", getObjectid() + ""));
		out.append(XMLWritingUtils.writeTag("date", getDate().getTime() + ""));
		out.append(getUser().toBasicXML());
		out.append("</recent>");
		
		return out.toString();
	}
	
	public static RecentlyAccessed fromXML(NativeElement element) {
		RecentlyAccessed accessed = new RecentlyAccessed();
		accessed.setId(Integer.valueOf(element.getAttribute("id")));
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if ("type".equals(node.getNodeName()))
				accessed.setType(node.getTextContent());
			else if ("objectid".equals(node.getNodeName()))
				accessed.setObjectid(Integer.valueOf(node.getTextContent()));
			else if ("date".equals(node.getNodeName()))
				accessed.setDate(new Date(Long.valueOf(node.getTextContent())));
			else if ("user".equals(node.getNodeName()))
				accessed.setUser(User.fromXML((NativeElement)node));
		}
		
		return accessed;
	}	

}
