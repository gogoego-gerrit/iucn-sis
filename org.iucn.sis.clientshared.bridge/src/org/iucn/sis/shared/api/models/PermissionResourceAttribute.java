package org.iucn.sis.shared.api.models;
/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import java.io.Serializable;

import com.solertium.lwxml.shared.NativeElement;
public class PermissionResourceAttribute implements Serializable {
	
	public static final String ROOT_TAG = "permAtt";
	
	public static PermissionResourceAttribute fromXML(NativeElement element, Permission permission) {
		PermissionResourceAttribute att = new PermissionResourceAttribute();
		att.setPermission(permission);
		att.setId(Integer.valueOf(element.getElementsByTagName("id").elementAt(0).getTextContent()));
		att.setRegex(element.getElementsByTagName("regex").elementAt(0).getTextContent());
		att.setName(element.getElementsByTagName("name").elementAt(0).getTextContent());
		return att;
	}
	
	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<" + ROOT_TAG + ">");
		xml.append("<id>" + getId() + "</id>");
		xml.append("<regex><![CDATA[" + getRegex() + "]]></regex>");
		xml.append("<name><![CDATA[" + getName() + "]]></name>");
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();		
	}
	
	public PermissionResourceAttribute() {
	}
	
	public PermissionResourceAttribute(String name, String regex) {
		this(name, regex, null);
	}
	
	public PermissionResourceAttribute(String name, String regex, Permission permission) {
		this.name = name;
		this.regex = regex;
		this.permission = permission;
	}
	
	private int id;
	
	private String name;
	
	private String regex;
	
	private Permission permission;
	
	public void setId(int value) {
		this.id = value;
	}
	
	public int getId() {
		return id;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setRegex(String value) {
		this.regex = value;
	}
	
	public String getRegex() {
		return regex;
	}
	
	public void setPermission(Permission value) {
		this.permission = value;
	}
	
	public Permission getPermission() {
		return permission;
	}
	
	@Override
	public String toString() {
		return getName() + ": " + getRegex();
	}

	
}
