package org.iucn.sis.shared.api.integrity;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.panels.integrity.IntegrityQuery;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.query.SelectedField;

public class SISQBQuery extends GWTQBQuery implements IntegrityQuery {
	
	private final Map<String, String> errorMessages;
	private final Map<String, String> properties;

	public SISQBQuery() {
		super();
		conditions = new SISQBConstraintGroup();
		errorMessages = new HashMap<String, String>();
		properties = new HashMap<String, String>();
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value == null ? defaultValue : value;
	}
	
	public void setErrorMessage(String comparisonID, String message) {
		errorMessages.put(comparisonID, message);
	}
	
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
	
	public void load(NativeNode element) {
		super.load(element);
		NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeElement cur = nodes.elementAt(i);
			if (cur.getNodeName().equals("errors")) {
				final NativeNodeList children = cur.getElementsByTagName("message");
				for (int k = 0; k < children.getLength(); k++) {
					final NativeElement curChild = children.elementAt(k);
					if ("message".equals(curChild.getNodeName()))
						errorMessages.put(curChild.getAttribute("id"), curChild.getTextContent());
				}
			}
			else if (cur.getNodeName().equals("properties")) {
				final NativeNodeList children = cur.getElementsByTagName("property");
				for (int k = 0; k < children.getLength(); k++) {
					final NativeElement curChild = children.elementAt(k);
					if ("property".equals(curChild.getNodeName()))
						properties.put(curChild.getAttribute("name"), curChild.getTextContent());
				}
			}
		}
	}
	
	public String toXML() {
		String xml = "<query version=\"1.1\">\r\n";
		for (int i = 0; i < fields.size(); i++) {
			SelectedField field = fields.get(i);
			xml += field.toXML() + "\r\n";
		}
		for (int i = 0; i < tables.size(); i++) {
			xml += "<table>" + tables.get(i) + "</table>\r\n";
		}
		//TODO: save arbitrary joins...
		xml += conditions.saveConfig();
		
		if (!errorMessages.isEmpty()) {
			xml += "\r\n<errors>\r\n";
			for (Map.Entry<String, String> entry : errorMessages.entrySet())
				xml += "<message id=\"" + entry.getKey() + "\"><![CDATA[" + entry.getValue() + "]]></message>\r\n";				
			xml += "</errors>";
		}
		
		if (!properties.isEmpty()) {
			xml += "\r\n<properties>\r\n";
			for (Map.Entry<String, String> entry : properties.entrySet())
				xml += "<property name=\"" + entry.getKey() + "\"><![CDATA[" + entry.getValue() + "]]></property>\r\n";
			xml += "</properties>";
		}
		
		xml += "\r\n</query>";
		return xml;
	}

	public String getErrorMessage(String comparisonID) {
		return errorMessages.get(comparisonID);
	}
	
}
