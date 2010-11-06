package org.iucn.sis.shared.api.models;


import java.io.Serializable;
import java.util.HashMap;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class PermissionGroup implements Serializable {

	
	private static final long serialVersionUID = 1L;
	public final static String ROOT_TAG = "permGroup";
	public static final String DEFAULT_PERMISSION_URI = "default";

	public static PermissionGroup fromXML(NativeNode element) {
		final PermissionGroup group = new PermissionGroup();
		final NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if ("name".equals(node.getNodeName()))
				group.setName(node.getTextContent());
			else if ("id".equals(node.getNodeName()))
				group.setId(Integer.parseInt(node.getTextContent()));
			else if ("scopeURI".equals(node.getNodeName()))
				group.setScopeURI(node.getTextContent());
			else if (Permission.ROOT_TAG.equals(node.getNodeName()))
				group.getPermissions().add(Permission.fromXML((NativeElement)node, group));	
			else if (User.ROOT_TAG.equals(node.getNodeName()))
				group.getUsers().add(User.fromXML((NativeElement)node));
			else if ("parent".equals(node.getNodeName())) {
				NativeElement el = (NativeElement)node;
				PermissionGroup parent = new PermissionGroup();
				parent.setId(Integer.valueOf(el.getAttribute("id")));
				parent.setName(el.getTextContent());
				group.setParent(parent);		
			}
		}

		return group;
	}

	

	private int id;

	private String name;

	private String scopeURI;

	private PermissionGroup parent;

	private java.util.Set<User> users;

	private java.util.Set<PermissionGroup> children;

	private java.util.Set<Permission> permissions;
	
	private HashMap<String, Permission> resourceToPermission;

	public PermissionGroup() {
		this.scopeURI = null;
		this.children = new java.util.HashSet<PermissionGroup>();
		this.permissions = new java.util.HashSet<Permission>();
		this.users = new java.util.HashSet<User>();
	}

	public PermissionGroup(String name) {
		this();
		this.name = name;
	}
	
	public boolean getDefaultPermission(String operation) {
		if (getResourceToPermission().containsKey(DEFAULT_PERMISSION_URI)) {
			return getResourceToPermission().get(DEFAULT_PERMISSION_URI).check(operation);
		} else 
			return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PermissionGroup)
			return getName().equals(((PermissionGroup) obj).getName());
		else
			return super.equals(obj);
	}

	public java.util.Set<PermissionGroup> getChildren() {
		return children;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getORMID() {
		return getId();
	}

	public PermissionGroup getParent() {
		return parent;
	}

	public java.util.Set<Permission> getPermissions() {
		return permissions;
	}
	
	/**
	 * This does a full resource to permission for itself and 
	 * its parents permissions, and its parents parents.... etc
	 * 
	 * @return
	 */
	public HashMap<String, Permission> getResourceToPermission() {
		if (resourceToPermission == null) {
			resourceToPermission = new HashMap<String, Permission>();
			PermissionGroup group = this;
			while (group != null) {
				for (Permission perm : group.getPermissions()) {
					if (!resourceToPermission.containsKey(perm.getUrl())) {
						resourceToPermission.put(perm.getUrl(), perm);
					}					
				}
				group = group.getParent();	
			}
			
		}
		return resourceToPermission;			
	}

	public String getScopeURI() {
		return scopeURI;
	}

	/**
	 * Gets the true scope of the permission group
	 * 
	 * @return
	 */
	public String getScope() {
		String scope = null;		
		PermissionGroup current = this;
		while ((scope == null || scope.trim().equalsIgnoreCase("")) && current != null) {
			scope = current.getScopeURI();
			current = current.getParent();
		}
		return scope;		
	}

	public java.util.Set<User> getUsers() {
		return users;
	}

	public void setChildren(java.util.Set<PermissionGroup> children) {
		this.children = children;
	}

	private void setId(int value) {
		this.id = value;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setParent(PermissionGroup parent) {
		this.parent = parent;
	}

	public void setPermissions(java.util.Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public void setScopeURI(String scopeURI) {
		this.scopeURI = scopeURI;
	}

	public void setUsers(java.util.Set<User> value) {
		this.users = value;
	}

	public String toBasicXML() {
		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">");
		xml.append("<id>" + getId() + "</id>");
		xml.append("<scopeURI>" + (getScopeURI() == null? "" : getScopeURI()) + "</scopeURI>");
		xml.append("<name>" + getName() + "</name>");
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();
	}

	public String toString() {
		return String.valueOf(getId());
	}

	// SHOULD BE CALLED AT LOWEST LEVEL
	// IE. THIS WILL NOT APPEND ANY OF IT'S CHILDREN.
	public String toXML() {
		StringBuffer xml = new StringBuffer("<" + ROOT_TAG + ">");
		xml.append("<id>" + getId() + "</id>");
		xml.append("<scopeURI>" + (getScopeURI() == null? "" : getScopeURI()) + "</scopeURI>");
		xml.append("<name><![CDATA[" + getName() + "]]></name>");
		for (Permission permission : getPermissions()) {
			xml.append(permission.toXML());
		}

		if (getParent() != null) {
			xml.append("<parent id=\"" + getParent().getId() + "\">");
			xml.append("<![CDATA[" + getParent().getName() + "]]>");
			xml.append("</parent>");
		}

		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();
	}
	
	public void removePermission(String resource) {
		Permission perm = getResourceToPermission().remove(resource);
		permissions.remove(perm);
	}
	
	public void addPermission(Permission permission) {
		permissions.add(permission);
		resourceToPermission = null;
	}

}
