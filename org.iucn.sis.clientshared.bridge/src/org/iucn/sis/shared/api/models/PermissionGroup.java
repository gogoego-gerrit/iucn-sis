package org.iucn.sis.shared.api.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableAssessmentShim;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class PermissionGroup implements Serializable {

	
	private static final long serialVersionUID = 1L;
	public final static String ROOT_TAG = "permGroup";
	public static final String DEFAULT_PERMISSION_URI = "default";

	public static PermissionGroup fromXML(NativeElement element) {

		PermissionGroup group = new PermissionGroup();
		group.setName(element.getElementsByTagName("name").elementAt(0).getTextContent());
		group.setId(Integer.parseInt(element.getElementsByTagName("id").elementAt(0).getText()));
		group.setScopeURI(element.getElementsByTagName("scopeURI").elementAt(0).getTextContent());

		NativeNodeList permissions = element.getElementsByTagName(Permission.ROOT_TAG);
		for (int i = 0; i < permissions.getLength(); i++) {
			group.getPermissions().add(Permission.fromXML(permissions.elementAt(i), group));
		}

		NativeNodeList users = element.getElementsByTagName(User.ROOT_TAG);
		for (int i = 0; i < users.getLength(); i++)
			group.getUsers().add(User.fromXML(users.elementAt(i)));

		NativeNodeList parents = element.getElementsByTagName("parent");
		if (parents.getLength() == 1) {
			PermissionGroup parent = new PermissionGroup();
			parent.setId(Integer.valueOf(parents.elementAt(0).getAttribute("id")));
			parent.setName(parents.elementAt(0).getTextContent());
			group.setParent(parent);
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
	
	/**
	 * 		
	 * @param object
	 * @param operation
	 * @return
	 */
	public boolean checkMe(AuthorizableObject object, String operation) {
		boolean hasPermission = getDefaultPermission(operation);
		
		if (object != null && object != null && isInScope(object)) {			
			String uri = object.getFullURI();
			boolean found = false;			
			while (uri.indexOf("/") > -1 && !found) {
				if (getResourceToPermission().containsKey(uri)) {
					Permission perm = getResourceToPermission().get(uri);
					hasPermission = perm.check(operation);
					if (hasPermission) {
						for (PermissionResourceAttribute cur : perm.getAttributes()) {
							if (cur != null && cur.getName() != null && cur.getRegex() != null)
								hasPermission = object.getProperty(cur.getName()).matches(cur.getRegex());
						}
					}
					found = true;
				} else {
					uri = uri.substring(0, uri.lastIndexOf("/"));
				}
			}
		}
		return hasPermission;
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
			System.out.println("in getResourceTOPermission");
			resourceToPermission = new HashMap<String, Permission>();
			PermissionGroup group = this;
			while (group != null) {
				System.out.println("the size of permissions is for " + group.getName() + "is "  + group.getPermissions());
				for (Permission perm : group.getPermissions()) {
					System.out.println("in permission " + perm.getUrl());
					if (!resourceToPermission.containsKey(perm.getUrl())) {
						System.out.println("for perm group " + group.getName() + " adding perission for " + perm.getUrl());
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
		System.out.println("this is scope " + scope);
		return scope;		
	}

	public java.util.Set<User> getUsers() {
		return users;
	}

	
	/**
	 * Returns whether the object fits within the criteria set forth by the scopeURI.  
	 * 
	 * @param object
	 * @return
	 */
	public boolean isInScope(AuthorizableObject object) {
		if (getScope() == null || getScope().trim().equals("")) {
			return true;
		}
		else if (object instanceof AuthorizableFeature || object instanceof WorkingSet || object instanceof Reference) {
			// Features, working sets and references aren't scoped
			return true;
		} else if (object instanceof Taxon) {
			Taxon taxon = (Taxon) object;

			if (getScope().startsWith("taxon")) {
				String[] split = getScope().split("/");
				int level = Integer.valueOf(split[1]);
				boolean kingdomMatch = true;

				if (split.length > 3 && taxon.getFootprint().length > 0)
					kingdomMatch = split[3].equalsIgnoreCase(taxon.getFootprint()[0]);

				if (taxon.getFootprint().length > level)
					return kingdomMatch && taxon.getFootprint()[level].equalsIgnoreCase(split[2]);
				else if (taxon.getLevel() == level)
					return kingdomMatch && taxon.getFullName().equalsIgnoreCase(split[2]);
				else
					return false;
			} else if (getScope().startsWith("workingSets")) {
				Map<Integer, WorkingSet> sets = WorkingSetCache.impl.getWorkingSets();
				for (Entry<Integer, WorkingSet> curEntry : sets.entrySet()) {
					if (curEntry.getValue().getSpeciesIDs().contains(taxon.getId() + ""))
						return true;
				}
				return false;
			} else if (getScope().startsWith("workingSet")) {
				String ids = getScope().substring(getScope().indexOf("/") + 1, getScope().length());
				Map<Integer, WorkingSet> sets = WorkingSetCache.impl.getWorkingSets();

				String[] split = ids.split(",");
				boolean ret = false;

				for (String cur : split) {
					if (sets.containsKey(Integer.valueOf(cur))) {
						ret = sets.get(Integer.valueOf(cur)).getSpeciesIDs().contains(taxon.getId());

						if (ret)
							return true;
					}
				}

				return false;
			} else
				return false;
		} else if (object instanceof Assessment) {
			if (getScope().startsWith("workingSet/")) {
				Assessment assessment = (Assessment) object;
				String ids = getScope().substring(getScope().indexOf("/") + 1, getScope().length());
				Map<Integer, WorkingSet> sets = WorkingSetCache.impl.getWorkingSets();

				String[] split = ids.contains(",") ? ids.split(",") : new String[] { ids };

				for (String curStr : split) {
					Integer cur = Integer.valueOf(curStr);

					if (sets.containsKey(cur)) {
						if (sets.get(cur).getSpeciesIDs().contains(assessment.getSpeciesID())) {
							Collection<Region> filterRegionList = sets.get(cur).getFilter().getRegions();
							List<Integer> filterRegionIDs = new ArrayList<Integer>();
							for (Region curReg : filterRegionList)
								filterRegionIDs.add(curReg.getId());

							if (sets.get(cur).getFilter().isAllRegions()) {
								return true;
							} else if (sets.get(cur).getFilter().getRegionType().equals(Relationship.AND)) {
								if (filterRegionIDs.containsAll(assessment.getRegionIDs())
										&& assessment.getRegionIDs().containsAll(filterRegionIDs)) {
									return true;
								}
							} else if (sets.get(cur).getFilter().getRegionType().equals(Relationship.OR)) {
								for (Integer curRegion : assessment.getRegionIDs()) {
									if (filterRegionIDs.contains(curRegion)) {
										return true;
									}
								}
							}
						}
					}
				}

				return false;
			} else {
				Taxon node = TaxonomyCache.impl.getTaxon(((Assessment) object).getSpeciesID());
				return isInScope(node);
			}
		} else if (object instanceof AuthorizableAssessmentShim) {
			AuthorizableAssessmentShim shim = (AuthorizableAssessmentShim) object;
			return isInScope(shim.getTaxon());
		} else
			return false; // Invalid scope URI
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
