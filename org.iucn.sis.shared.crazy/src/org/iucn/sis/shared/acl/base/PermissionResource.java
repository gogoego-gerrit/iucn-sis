package org.iucn.sis.shared.acl.base;

import java.util.HashMap;
import java.util.Map.Entry;

public class PermissionResource {

	private String uri;
	private PermissionSet set;
	private HashMap<String, String> attributes;
	
	public PermissionResource(String uri) {
		this(uri, new PermissionSet());
	}
	
	public PermissionResource(String uri, PermissionSet set) {
		this.uri = uri;
		this.set = set;
		attributes = new HashMap<String, String>();
	}
	
	public PermissionResource deepCopy() {
		PermissionResource r = new PermissionResource(uri, new PermissionSet(new HashMap<String, Boolean>(set.getOperationToAllowed())));
		r.setAttributes(new HashMap<String, String>(attributes));
		return r;
	}
	
	public Boolean check(String operation) {
		return set.check(operation);
	}
	
	public String getURI() {
		return uri;
	}
	
	public void setURI(String uri) {
		this.uri = uri;
	}
	
	public PermissionSet getSet() {
		return set;
	}
	
	public void setSet(PermissionSet set) {
		this.set = set;
	}
	
	public HashMap<String, String> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public String toXML() {
		StringBuilder ret = new StringBuilder();
		
		String resourceURI = uri.substring(uri.indexOf('/', 0)+1, uri.length());
		String resourceType = uri.substring(0, uri.indexOf('/', 0));
		
		ret.append("<" + resourceType + " uri=\"");
		ret.append(resourceURI);
		ret.append("\" ");
		
		for(Entry<String, String> attr : getAttributes().entrySet() ) {
			ret.append(attr.getKey() + "=\"");
			ret.append(attr.getValue() + "\" ");
		}
		
		ret.append(">");
		ret.append(set.toString());
		ret.append("</" + resourceType + ">\r\n");
		
		return ret.toString();
	}
}
