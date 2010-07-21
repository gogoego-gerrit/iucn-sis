package org.iucn.sis.client.acl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.shared.acl.User;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.acl.base.PermissionGroup;
import org.iucn.sis.shared.acl.base.PermissionParser;
import org.iucn.sis.shared.data.WorkingSetData;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

public class AuthorizationCache {

	public static AuthorizationCache impl = new AuthorizationCache();
	
	private PermissionParser parser;

	private HashMap<String, PermissionGroup> groups;
	private HashMap<User, List<PermissionGroup>> permissionMaps;
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
			ndoc.delete("/acl/group/" + groupName, new GenericCallback<String>() {
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
			ndoc.delete("/acl/group/" + groupNames, new GenericCallback<String>() {
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
		ndoc.post("/acl/groups", str.toString(), new GenericCallback<String>() {
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
		groups = null;
	}

	/**
	 * Initializes the cache by fetching the permission groups and populating itself with
	 * them. If credentials are required to fetch the groups, you should first set them
	 * via the setCredentials(...) call on this object.
	 */
	public void init() {
		if( permissionMaps == null ) {
			permissionMaps = new HashMap<User, List<PermissionGroup>>();

			final NativeDocument ndoc = getNativeDocument();
			ndoc.get("/acl/groups", new GenericCallback<String>() {
				public void onSuccess(String result) {
					parser = new PermissionParser(ndoc);
					groups = parser.getGroups();
				}

				public void onFailure(Throwable caught) {}
			});
		}
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
			permissionMaps = new HashMap<User, List<PermissionGroup>>();
			final NativeDocument ndoc = getNativeDocument();
			ndoc.get("/acl/groups", new GenericCallback<String>() {
				public void onSuccess(String result) {
					parser = new PermissionParser(ndoc);
					groups = parser.getGroups();
					initCallback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					initCallback.onFailure(caught);
				}
			});
		}
	}

	public void addUser(User user) {
		try {
			ArrayList<PermissionGroup> userPGs = new ArrayList<PermissionGroup>();

			String [] userGroups;
			if( user.getProperty("quickGroup") == null )
				userGroups = new String[] { "sysAdmin" };
			else if( user.getProperty("quickGroup").indexOf(",") > -1 )
				userGroups = user.getProperty("quickGroup").split(",");
			else
				userGroups = new String[] { user.getProperty("quickGroup") };

			List<String> groupList = Arrays.asList(userGroups);
			boolean defaultFound = false;
			for( String groupName : groupList ) {
				PermissionGroup curGroup = parser.getGroup(groupName.replaceAll("'", ""));
				if( curGroup != null ) {
					userPGs.add(curGroup);
					if( curGroup.getResources().containsKey(PermissionGroup.DEFAULT_PERMISSION_URI))
						defaultFound = true;
				}
				else
					System.out.println("Unable to find group " + groupName.replaceAll("'", ""));
			}

			if( !defaultFound )
				userPGs.add(parser.getGroup("basic"));
			
			permissionMaps.put(user, userPGs);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean hasRight(User user, String operation, AuthorizableObject auth) {
		if (permissionMaps.containsKey(user)) {
			boolean ret = false;

			//If it's a working set, test ownership first to escape early
			if( auth instanceof WorkingSetData && 
					user.getUsername().equalsIgnoreCase(((WorkingSetData)auth).getCreator() ) )
				return true;

			for( PermissionGroup curGroup : permissionMaps.get(user) ) {
				ret = ret || curGroup.check(auth, operation);
				if( ret ) //SHORT CIRCUIT!
					break;
			}

			return ret;
		}
		else
			throw new RuntimeException("No permissions for user " + user.getUsername()); 
	}

}
