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
import java.util.HashSet;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
public class Permission implements Serializable {
	
	public static final String ROOT_TAG = "perm";
	
	public Permission(String uri) {
		this(uri, false, false, false, false, false, false);
	}
	
	public Permission(String uri, boolean read, boolean write, boolean create, boolean delete,
			boolean grant, boolean use) {
		this.url = uri;
		setRead(read);
		setWrite(write);
		setCreate(create);
		setDelete(delete);
		setGrant(grant);
		setUse(use);
	}
	
	
	public Permission deepCopy() {
		Permission r = new Permission();
		r.setUrl(getUrl());
		r.setType(getType());
		r.setPermissionGroup(getPermissionGroup());
		r.setRead(isRead());
		r.setWrite(isWrite());
		r.setCreate(isCreate());
		r.setGrant(isGrant());
		r.setUse(isUse());
		r.attributes = new HashSet<PermissionResourceAttribute>(getAttributes());
		return r;
	}
	
	public Boolean check(String operation) {
		if( operation.equals(AuthorizableObject.READ))
			return read;
		else if( operation.equals(AuthorizableObject.WRITE))
			return write;
		else if( operation.equals(AuthorizableObject.CREATE))
			return create;
		else if( operation.equals(AuthorizableObject.DELETE))
			return delete;
		else if( operation.equals(AuthorizableObject.GRANT))
			return grant;
		else if( operation.equals(AuthorizableObject.USE_FEATURE))
			return use;
		else 
			return null;
	}
	
	/**
	 * Returns the xml of the Permission.  Does not include information
	 * on the permission group, as this should be called only within the
	 * toXML of the permissionGroup
	 * 
	 * @return
	 */
	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<" + ROOT_TAG + ">");
		xml.append("<id>" + getId() + "</id>");
		xml.append("<url><![CDATA[" + getUrl() + "]]></url>");
		xml.append("<type>" + ((getType()==null)? "" : getType() )+ "</type>");
		xml.append("<read>" + isRead() + "</read>");
		xml.append("<write>" + isWrite() + "</write>");
		xml.append("<create>" + isCreate() + "</create>");
		xml.append("<delete>" + isDelete() + "</delete>");
		xml.append("<grant>" + isGrant() + "</grant>");
		xml.append("<use>" + isUse() + "</use>");
		for (PermissionResourceAttribute att : getAttributes()) {
			xml.append(att.toXML());
		}
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();

	}
	
	/**
	 * Returns Permission model from xml representation
	 * 
	 * @param element
	 * @param group
	 * @return
	 */
	public static Permission fromXML(NativeElement element, PermissionGroup group) {
		Permission permission = new Permission();
		permission.setPermissionGroup(group);
		permission.setId(Integer.valueOf(element.getElementsByTagName("id").elementAt(0).getTextContent()));
		permission.setUrl(element.getElementsByTagName("url").elementAt(0).getTextContent());
		permission.setType(element.getElementsByTagName("type").elementAt(0).getTextContent());
		permission.setRead("true".equalsIgnoreCase(element.getElementsByTagName("read").elementAt(0).getTextContent()));
		permission.setWrite("true".equalsIgnoreCase(element.getElementsByTagName("write").elementAt(0).getTextContent()));
		permission.setCreate("true".equalsIgnoreCase(element.getElementsByTagName("create").elementAt(0).getTextContent()));
		permission.setGrant("true".equalsIgnoreCase(element.getElementsByTagName("grant").elementAt(0).getTextContent()));
		permission.setDelete("true".equalsIgnoreCase(element.getElementsByTagName("delete").elementAt(0).getTextContent()));
		permission.setUse("true".equalsIgnoreCase(element.getElementsByTagName("use").elementAt(0).getTextContent()));
	
		NativeNodeList atts = element.getElementsByTagName(PermissionResourceAttribute.ROOT_TAG);
		for (int i = 0; i < atts.getLength(); i++) {
			permission.attributes.add(PermissionResourceAttribute.fromXML(atts.elementAt(i), permission));
		}
		
		return permission;
		
	}
	
	
	public Permission() {
	}
	
	private int id;
	
	private String url;
	
	private String type;
	
	private boolean read;
	
	private boolean write;
	
	private boolean create;
	
	private boolean delete;
	
	private boolean grant;
	
	private boolean use;
	
	private PermissionGroup permissionGroup;
		
	private java.util.Set<PermissionResourceAttribute> attributes = new java.util.HashSet<PermissionResourceAttribute>();
	
	public void setId(int value) {
		this.id = value;
	}
	
	public int getId() {
		return id;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public void setUrl(String value) {
		this.url = value;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setPermissionGroup(PermissionGroup value) {
		this.permissionGroup = value;
	}
	
	public PermissionGroup getPermissionGroup() {
		return permissionGroup;
	}
	
	
	public String toString() {
		return "For #" + getId() + " @ "+ getUrl() + ", " +
			"R" + plusOrMinus(read) + ", " + 
			"W" + plusOrMinus(write) + ", " +
			"C" + plusOrMinus(create) + ", " + 
			"D" + plusOrMinus(delete) + ", " + 
			"G" + plusOrMinus(grant) + ", " + 
			"U" + plusOrMinus(use); 
	}
	
	private String plusOrMinus(boolean value) {
		return value ? "+" : "-";
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isGrant() {
		return grant;
	}

	public void setGrant(boolean grant) {
		this.grant = grant;
	}

	public boolean isUse() {
		return use;
	}

	public void setUse(boolean use) {
		this.use = use;
	}

	public void setAttributes(java.util.Set<PermissionResourceAttribute> attributes) {
		this.attributes = attributes;
	}
	
	public java.util.Set<PermissionResourceAttribute> getAttributes() {
		return attributes;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
}
