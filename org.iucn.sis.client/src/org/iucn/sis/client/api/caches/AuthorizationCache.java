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
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.PermissionUtils;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class AuthorizationCache {

	public static AuthorizationCache impl = new AuthorizationCache();


	private HashMap<Integer, PermissionGroup> idToGroups;
	private HashMap<String, PermissionGroup> groups;
	private HashMap<User, Set<PermissionGroup>> permissionMaps;
	private String credentials;
	
	private AuthorizationCache() {}

	public HashMap<String, PermissionGroup> getGroups() {
		return groups;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}
	
	private NativeDocument getNativeDocument() {
		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
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
		
		NativeDocument ndoc = getNativeDocument();
		ndoc.post(UriBase.getInstance() + "/acl/groups", str.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				for( PermissionGroup group : groupList )
					groups.put(group.getName(), group);
				
				saveCallback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				saveCallback.onFailure(caught);
			}
		});
	}

	public void clear() {
		permissionMaps = null;
		idToGroups = null;
		groups = null;
	}

	/**
	 * Initializes the cache by fetching the permission groups and populating itself with
	 * them. If credentials are required to fetch the groups, you should first set them
	 * via the setCredentials(...) call on this object.
	 */
	public void init() {
		this.init(null);
	}

	/**
	 * Initializes the cache by fetching the permission groups and populating itself with
	 * them. If credentials are required to fetch the groups, you should first set them
	 * via the setCredentials(...) call on this object.
	 * 
	 * @param initCallback - invokes this callback when done
	 */
	public void init(final GenericCallback<String> initCallback) {
		if( permissionMaps == null ) {
			permissionMaps = new HashMap<User, Set<PermissionGroup>>();
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
					
					final NativeDocument userDoc = getNativeDocument();
					userDoc.get(UriBase.getInstance() + "/acl/user/" + SISClientBase.currentUser.getUsername() , new GenericCallback<String>() {
					
						@Override
						public void onSuccess(String result) {
							NativeNodeList list = userDoc.getDocumentElement().getElementsByTagName(PermissionGroup.ROOT_TAG);
							Set<PermissionGroup> perms = new HashSet<PermissionGroup>();
							SISClientBase.currentUser.getPermissionGroups().clear();
							
							for (int i = 0; i < list.getLength(); i++) {
								PermissionGroup group = PermissionGroup.fromXML(list.elementAt(i));
								SISClientBase.currentUser.getPermissionGroups().add(groups.get(group.getName()));
							}
							
							addUser(SISClientBase.currentUser);
							
							if (initCallback != null)
								initCallback.onSuccess(result);
					
						}
					
						@Override
						public void onFailure(Throwable caught) {
							if (initCallback != null)
								initCallback.onFailure(caught);					
						}
						
					});
				}

				public void onFailure(Throwable caught) {
					if (initCallback != null)
						initCallback.onFailure(caught);
				}				
				
			});
		}
	}

	public void addUser(ClientUser user) {
		try {
			
			if (user.getPermissionGroups().isEmpty()) {
				user.getPermissionGroups().add(groups.get("guest"));
			}
			permissionMaps.put(user, user.getPermissionGroups());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean hasRight(ClientUser user, String operation, AuthorizableObject auth) {
		if (permissionMaps.containsKey(user)) {			
			boolean ret = false;

			//If it's a working set, test ownership first to escape early
			if( auth instanceof WorkingSet && 
					user.getUsername().equalsIgnoreCase(((WorkingSet)auth).getCreator().getUsername() ) )
				return true;

			for( PermissionGroup curGroup : permissionMaps.get(user) ) {
				ret = ret || PermissionUtils.checkMe(curGroup, auth, operation);
				if( ret ) //SHORT CIRCUIT!
					break;
			}
			return ret;
		}
		else
			throw new RuntimeException("No permissions for user " + user.getUsername()); 
	}
	
	public HashMap<Integer, PermissionGroup> getIdToGroups() {
		return idToGroups;
	}

}
