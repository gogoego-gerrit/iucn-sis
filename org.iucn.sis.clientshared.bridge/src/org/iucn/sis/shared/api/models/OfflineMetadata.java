package org.iucn.sis.shared.api.models;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class OfflineMetadata {
	
	private String name;
	private String location;
	private String lastModified;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
	public String toXML() {
		final StringBuilder out = new StringBuilder();
		out.append("<offline>");
		out.append(XMLWritingUtils.writeCDATATag("name", getName()));
		out.append(XMLWritingUtils.writeCDATATag("location", getLocation()));
		out.append(XMLWritingUtils.writeCDATATag("date", getLastModified()));
		out.append("</offline>");
		return out.toString();
	}
	
	public static OfflineMetadata fromXML(NativeElement element) {
		final OfflineMetadata metadata = new OfflineMetadata();
		
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if ("name".equals(node.getNodeName()))
				metadata.setName(node.getTextContent());
			else if ("location".equals(node.getNodeName()))
				metadata.setLocation(node.getTextContent());
			else if ("date".equals(node.getNodeName()))
				metadata.setLastModified(node.getTextContent());
		}	
		return metadata;
	}

}
