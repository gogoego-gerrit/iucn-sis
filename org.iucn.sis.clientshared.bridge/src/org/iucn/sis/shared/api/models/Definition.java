package org.iucn.sis.shared.api.models;

import java.io.Serializable;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class Definition implements Serializable, Comparable<Definition> {
	
	private int id;
	
	private String name;
	
	private String value;
	
	public Definition() {	
	}
	
	public Definition(String name, String value) {
		setName(name);
		setValue(value);
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String toXML() {
		StringBuilder out = new StringBuilder();
		out.append("<definition id=\"" + getId() + "\">");
		out.append(XMLWritingUtils.writeCDATATag("name", name));
		out.append(XMLWritingUtils.writeCDATATag("value", value, true));
		out.append("</definition>");
		
		return out.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Definition other = (Definition) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(Definition o) {
		return Integer.valueOf(id).compareTo(o.id);
	}

	public static Definition fromXML(NativeDocument document) {
		return fromXML(document.getDocumentElement());
	}
	
	public static Definition fromXML(NativeElement element) {
		final Definition definition = new Definition();
		try {
			definition.setId(Integer.valueOf(element.getAttribute("id")));
		} catch (NumberFormatException e) {
			definition.setId(0);
		}
		
		final NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if ("name".equals(node.getNodeName()))
				definition.setName(node.getTextContent());
			else if ("value".equals(node.getNodeName()))
				definition.setValue(node.getTextContent());
		}
		
		return definition;
	}
}
