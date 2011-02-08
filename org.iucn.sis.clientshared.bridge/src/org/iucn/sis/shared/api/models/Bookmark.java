package org.iucn.sis.shared.api.models;

import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class Bookmark {
	
	private int id;
	
	private String name;
	
	private String value;
	
	private Date date;
	
	private User user;

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public String toXML() {
		final StringBuilder out = new StringBuilder();
		out.append("<bookmark id=\"" + getId() + "\">");
		out.append(XMLWritingUtils.writeCDATATag("name", getName()));
		out.append(XMLWritingUtils.writeCDATATag("value", getValue()));
		out.append(XMLWritingUtils.writeTag("date", date.getTime() + ""));
		out.append(user.toBasicXML());
		out.append("</bookmark>");
		
		return out.toString();
	}
	
	public static Bookmark fromXML(NativeElement element) {
		final Bookmark bookmark = new Bookmark();
		bookmark.setId(Integer.valueOf(element.getAttribute("id")));
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if ("name".equals(node.getNodeName()))
				bookmark.setName(node.getTextContent());
			else if ("value".equals(node.getNodeName()))
				bookmark.setValue(node.getTextContent());
			else if ("date".equals(node.getNodeName()))
				bookmark.setDate(new Date(Long.parseLong(node.getTextContent())));
			else if (User.ROOT_TAG.equals(node.getNodeName()))
				bookmark.setUser(User.fromXML((NativeElement)node));
		}
		
		return bookmark;
	}
	

}
