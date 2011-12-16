package org.iucn.sis.shared.api.integrity;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.panels.integrity.IntegrityQuery;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class SQLQuery implements IntegrityQuery {
	
	private final Map<String, String> properties;
	
	private String joins;
	private String conditions;
	private String message;
	
	public SQLQuery() {
		super();
		properties = new HashMap<String, String>();
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value == null ? defaultValue : value;
	}
	
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
	
	public void setConditions(String conditions) {
		this.conditions = conditions;
	}
	
	public void setJoins(String joins) {
		this.joins = joins;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getConditions() {
		return conditions;
	}
	
	public String getJoins() {
		return joins;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void load(NativeNode element) {
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode cur = nodes.item(i);
			if (cur.getNodeName().equals("properties")) {
				final NativeNodeList children = cur.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					final NativeNode curChild = children.item(k);
					if ("property".equals(curChild.getNodeName()))
						properties.put(((NativeElement)curChild).getAttribute("name"), curChild.getTextContent());
				}
			}
			else if ("joins".equals(cur.getNodeName()))
				joins = cur.getTextContent();
			else if ("conditions".equals(cur.getNodeName()))
				conditions = cur.getTextContent();
			else if ("message".equals(cur.getNodeName()))
				message = cur.getTextContent();
		}
	}
	
	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<query type=\"sql\" version=\"1.1\">\r\n");
		xml.append(XMLWritingUtils.writeCDATATag("joins", joins, true));
		xml.append(XMLWritingUtils.writeCDATATag("conditions", conditions, true));
		
		if (message != null)
			xml.append("<message id=\"sql\"><![CDATA[" + message + "]]></message>");
		
		if (!properties.isEmpty()) {
			xml.append("\r\n<properties>\r\n");
			for (Map.Entry<String, String> entry : properties.entrySet())
				xml.append("<property name=\"" + entry.getKey() + "\"><![CDATA[" + entry.getValue() + "]]></property>\r\n");
			xml.append("</properties>");
		}
		
		xml.append("\r\n</query>");
		return xml.toString();
	}
	
}
