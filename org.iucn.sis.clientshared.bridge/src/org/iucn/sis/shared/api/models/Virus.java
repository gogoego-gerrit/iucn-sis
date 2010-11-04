package org.iucn.sis.shared.api.models;

import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class Virus {
	
	private int id;
	private String name;
	private String comments;	
	private User user;
	private Date added;
	
	private long generationID;
	
	public Virus() {
		generationID = new Date().getTime();
	}
	
	public static Virus fromXML(NativeNode node) {
		final Virus virus = new Virus();
		
		final NativeNodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode current = nodes.item(i);
			String name = current.getNodeName(), value = current.getTextContent();
			if ("id".equals(name))
				virus.setId(Integer.parseInt(value));
			else if ("name".equals(name))
				virus.setName(value);
			else if ("comments".equals(name))
				virus.setComments(value);
			else if ("user".equals(name))
				virus.setUser(User.fromXML((NativeElement)current));
			else if ("added".equals(name))
				virus.setAdded(new Date(Long.parseLong(value)));
		}
		
		return virus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
		this.generationID = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getAdded() {
		return added;
	}

	public void setAdded(Date added) {
		this.added = added;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (generationID ^ (generationID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Virus other = (Virus) obj;
		if (generationID != other.generationID)
			return false;
		return true;
	}

	public String toXML() {
		StringBuilder out = new StringBuilder();
		out.append("<virus>");
		out.append(XMLWritingUtils.writeTag("id", getId()+""));
		out.append(XMLWritingUtils.writeTag("name", name));
		out.append(XMLWritingUtils.writeTag("comments", comments));
		out.append(XMLWritingUtils.writeTag("date", String.valueOf(added.getTime())));
		out.append(user.toXML());
		out.append("</virus>");
		
		return out.toString();
	}

}
