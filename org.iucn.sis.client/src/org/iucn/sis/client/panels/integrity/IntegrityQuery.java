package org.iucn.sis.client.panels.integrity;

import com.solertium.lwxml.shared.NativeNode;

public interface IntegrityQuery {
	
	public String getProperty(String key);
	
	public String getProperty(String key, String defaultValue);
	
	public void load(NativeNode element);
	
	public void setProperty(String key, String value);
	
	public String toXML();

}
