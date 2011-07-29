package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.PermissionUtils;

import com.google.gwt.core.client.GWT;
import com.solertium.lwxml.gwt.NativeDocumentImpl;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class AuthorizationCache {

	public static final AuthorizationCache impl = new AuthorizationCache();

	private HashMap<Integer, PermissionGroup> idToGroups;
	private HashMap<String, PermissionGroup> groups;
	private Set<PermissionGroup> permissionGroups = null;
	private String credentials;
	
	private AuthorizationCache() {}

	public HashMap<String, PermissionGroup> getGroups() {
		return groups;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}
	
	public NativeDocument getNativeDocument() {
		NativeDocument doc = new NativeDocumentImpl();
		doc.setHeader("Authorization", "Basic " + credentials);
		
		return doc;
	}
	
	/**
	 * 
	 * @param groupName
	 * @param saveCallback
	 * @return the removed group
	 */
	public void removeGroup(final String groupName, final GenericCallback<PermissionGroup> saveCallback) {
		if( saveCallback != null ) {
			NativeDocument ndoc = getNativeDocument();
			ndoc.delete(UriBase.getInstance() + "/acl/group/" + groupName, new GenericCallback<String>() {
				public void onSuccess(String result) {
					PermissionGroup group =  groups.remove(groupName);
					saveCallback.onSuccess(group);
				}
				public void onFailure(Throwable caught) {
					saveCallback.onFailure(caught);
				}
			});
		}
	}
	
	/**
	 * Deletes multiple groups; supply group names as a CSV. 
	 * 
	 * @param groupNames
	 * @param saveCallback
	 * @return null
	 */
	public void removeGroups(final String groupNames, final GenericCallback<String> saveCallback) {
		if( saveCallback != null ) {
			NativeDocument ndoc = getNativeDocument();
			ndoc.delete(UriBase.getInstance() + "/acl/group/" + groupNames, new GenericCallback<String>() {
				public void onSuccess(String result) {
					for( String cur : groupNames.split(",") )
						groups.remove(cur);
					
					saveCallback.onSuccess(null);
				}
				public void onFailure(Throwable caught) {
					saveCallback.onFailure(caught);
				}
			});
		}
	}

	/**
	 * Saves group changes.
	 * 
	 * @param group
	 * @param saveCallback - callback, null if save to server is unwanted
	 */
	public void saveGroup(final PermissionGroup group, final GenericCallback<String> saveCallback) {
		ArrayList<PermissionGroup> list = new ArrayList<PermissionGroup>();
		list.add(group);
		saveGroups(list, saveCallback);
	}

	/**
	 * Saves group changes.
	 * 
	 * @param group
	 * @param saveCallback
	 */
	public void saveGroups(final List<PermissionGroup> groupList, final GenericCallback<String> saveCallback) {
		StringBuilder str = new StringBuilder("<groups>\n");
		for( PermissionGroup group : groupList )
			str.append(group.toXML());
		str.append("</groups>");
		
		final NativeDocument ndoc = getNativeDocument();
		ndoc.post(UriBase.getInstance() + "/acl/groups", str.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final NativeNodeList nodes = ndoc.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					final NativeNode node = nodes.item(i);
					if (PermissionGroup.ROOT_TAG.equals(node.getNodeName())) {
						PermissionGroup group = PermissionGroup.fromXML(node);
						if (groups.containsKey(group.getName()))
							groups.get(group.getName()).setID(group.getId());
						
						groups.put(group.getName(), group);
					}
				}
				saveCallback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				saveCallback.onFailure(caught);
			}
		});
	}

	public void clear() {
		permissionGroups = null;
		idToGroups = null;
		groups = null;
	}

	/**
	 * Initializes the cache by fetching the permission groups and populating itself with
	 * them. If credentials are required to fetch the groups, you should first set them
	 * via the setCredentials(...) call on this object.
	 * 
	 * @param initCallback - invokes this callback when done
	 */
	public void init(final GenericCallback<String> initCallback) {
		idToGroups = new HashMap<Integer, PermissionGroup>();
		groups = new HashMap<String, PermissionGroup>();

		final NativeDocument ndoc = getNativeDocument();
		ndoc.get(UriBase.getInstance() + "/acl/groups", new GenericCallback<String>() {
			public void onSuccess(String result) {
				NativeNodeList groupEls = ndoc.getDocumentElement().getElementsByTagName(PermissionGroup.ROOT_TAG);
				for (int i = 0; i < groupEls.getLength(); i++) {
					PermissionGroup group = PermissionGroup.fromXML(groupEls.elementAt(i));
					groups.put(group.getName(), group);
					idToGroups.put(Integer.valueOf(group.getId()), group);
				}
				for (Entry<String, PermissionGroup> entry : groups.entrySet()) {
					if (entry.getValue().getParent() != null) {
						entry.getValue().setParent(idToGroups.get(Integer.valueOf(entry.getValue().getParent().getId())));
					}
				}
				
				if (initCallback != null)
					initCallback.onSuccess(result);
			}

			public void onFailure(Throwable caught) {
				if (initCallback != null)
					initCallback.onFailure(caught);
			}
		});
		
	}
	
	public PermissionGroup.PermissionGroupLocater getFinder() {
		return new PermissionGroup.PermissionGroupLocater() {
			public PermissionGroup findGroup(Integer id, String name) {
				PermissionGroup parent = idToGroups == null ? null : idToGroups.get(id);
				if (parent == null) {
					parent = new PermissionGroup();
					parent.setID(id);
					parent.setName(name);
				}
				return parent;
			}
		};
	}

	public void addUser(ClientUser user) {
		try {
			if (user.getPermissionGroups().isEmpty()) {
				user.getPermissionGroups().add(groups.get("guest"));
			}
			permissionGroups = new HashSet<PermissionGroup>(user.getPermissionGroups());
		} catch (Throwable e) {
			Debug.println(e);
			GWT.log("Permission Loading Error", e);
		}
	}
	
	public boolean hasRight(String operation, AuthorizableObject auth) {
		return hasRight(SISClientBase.currentUser, operation, auth);
	}

	public boolean hasRight(ClientUser user, String operation, AuthorizableObject auth) {
		boolean ret = false;

		//If it's a working set, test ownership first to escape early
		if( auth instanceof WorkingSet && 
				user.getUsername().equalsIgnoreCase(((WorkingSet)auth).getCreator().getUsername() ) )
			return true;

		for (PermissionGroup curGroup : permissionGroups) {
			ret |= PermissionUtils.checkMe(curGroup, auth, operation);
			if( ret ) //SHORT CIRCUIT!
				break;
		}
		return ret;
	}
	
	public HashMap<Integer, PermissionGroup> getIdToGroups() {
		return idToGroups;
	}

}
