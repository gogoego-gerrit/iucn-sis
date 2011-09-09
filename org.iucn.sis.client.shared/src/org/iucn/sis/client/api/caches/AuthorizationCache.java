package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
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

	private final Cache cache;
	
	private Set<PermissionGroup> permissionGroups = null;
	private String credentials;
	
	private AuthorizationCache() {
		cache = new Cache();
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
					PermissionGroup group =  cache.remove(groupName);
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
						cache.remove(cur);
					
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
		ndoc.put(UriBase.getInstance() + "/acl/groups", str.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final NativeNodeList nodes = ndoc.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					final NativeNode node = nodes.item(i);
					if (PermissionGroup.ROOT_TAG.equals(node.getNodeName())) {
						PermissionGroup group = PermissionGroup.fromXML(node);
						if (cache.containsKey(group.getName()))
							cache.get(group.getName()).setID(group.getId());
						
						cache.put(group);
					}
				}
				saveCallback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				saveCallback.onFailure(caught);
			}
		});
	}
	
	
	/**
	 * Update group changes.
	 * 
	 * @param group
	 * @param updateCallback
	 */
	public void updateGroup(final PermissionGroup group, final GenericCallback<PermissionGroup> updateCallback) {
		StringBuilder str = new StringBuilder("<groups>\n");
		str.append(group.toXML());
		str.append("</groups>");
		final NativeDocument ndoc = getNativeDocument();
		ndoc.post(UriBase.getInstance() + "/acl/groups", str.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				PermissionGroup group = null;
				final NativeNodeList nodes = ndoc.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					final NativeNode node = nodes.item(i);
					if (PermissionGroup.ROOT_TAG.equals(node.getNodeName())) {
						group = PermissionGroup.fromXML(node);
						if (cache.containsKey(group.getName()))
							cache.get(group.getName()).setID(group.getId());
						
						cache.put(group);
					}
				}
				updateCallback.onSuccess(group);
			}
			public void onFailure(Throwable caught) {
				updateCallback.onFailure(caught);
			}
		});
	}	

	public void clear() {
		permissionGroups = null;
		cache.clear();
	}

	/**
	 * Initializes the cache by fetching the permission groups and populating itself with
	 * them. If credentials are required to fetch the groups, you should first set them
	 * via the setCredentials(...) call on this object.
	 * 
	 * @param initCallback - invokes this callback when done
	 */
	public void init(final GenericCallback<String> initCallback) {
		cache.clear();
		
		final NativeDocument ndoc = getNativeDocument();
		ndoc.get(UriBase.getInstance() + "/acl/groups", new GenericCallback<String>() {
			public void onSuccess(String result) {
				NativeNodeList groupEls = ndoc.getDocumentElement().getElementsByTagName(PermissionGroup.ROOT_TAG);
				for (int i = 0; i < groupEls.getLength(); i++) {
					PermissionGroup group = PermissionGroup.fromXML(groupEls.elementAt(i));
					cache.put(group);
				}
				for (PermissionGroup group : cache.values()) {
					if (group.getParent() != null) {
						group.setParent(cache.get(Integer.valueOf(group.getParent().getId())));
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
				PermissionGroup parent = cache.get(id);
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
				user.getPermissionGroups().add(cache.get("guest"));
			}
			permissionGroups = new HashSet<PermissionGroup>(user.getPermissionGroups());
		} catch (Throwable e) {
			Debug.println(e);
			GWT.log("Permission Loading Error", e);
		}
	}
	
	public boolean canUse(AuthorizableFeature feature) {
		return hasRight(AuthorizableObject.USE_FEATURE, feature);
	}
	
	public boolean hasRight(String operation, AuthorizableObject auth) {
		return hasRight(SISClientBase.currentUser, operation, auth);
	}

	public boolean hasRight(ClientUser user, String operation, AuthorizableObject auth) {
		boolean ret = false;

		//If it's a working set, test ownership first to escape early
		if (auth instanceof WorkingSet) {
			WorkingSet ws = (WorkingSet)auth;
			if (ws.getCreator() != null && user.getUsername().equals(ws.getCreator().getUsername()))
				return true;
		}

		for (PermissionGroup curGroup : permissionGroups) {
			ret |= PermissionUtils.checkMe(curGroup, auth, operation);
			if( ret ) //SHORT CIRCUIT!
				break;
		}
		return ret;
	}
	
	public PermissionGroup getGroup(Integer id) {
		return cache.get(id);
	}
	
	public PermissionGroup getGroup(String name) {
		return cache.get(name);
	}
	
	public boolean hasGroup(String name) {
		return cache.containsKey(name);
	}
	
	public List<PermissionGroup> listGroups() {
		return new ArrayList<PermissionGroup>(cache.values());
	}
	
	public void sync(PermissionGroup target) {
		PermissionGroup source = getGroup(target.getName());
		if (source != null) {
			target.setID(source.getId());
		}
		
		cache.put(target);
	}
	
	private static class Cache {
		
		private final Map<String, PermissionGroup> nameCache;
		private final Map<Integer, PermissionGroup> idCache;
		
		public Cache() {
			nameCache = new HashMap<String, PermissionGroup>();
			idCache = new HashMap<Integer, PermissionGroup>();
		}
		
		public PermissionGroup get(String name) {
			return nameCache.get(name);
		}
		
		public PermissionGroup get(Integer id) {
			return idCache.get(id);
		}
		
		public void clear() {
			nameCache.clear();
			idCache.clear();
		}
		
		public PermissionGroup remove(String name) {
			return nameCache.remove(name);
		}
		
		public PermissionGroup remove(Integer id) {
			return idCache.remove(id);
		}
		
		public boolean containsKey(String name) {
			return nameCache.containsKey(name);
		}
		
		public boolean containsKey(Integer id) {
			return idCache.containsKey(id);
		}
		
		public void put(PermissionGroup group) {
			nameCache.put(group.getName(), group);
			idCache.put(group.getId(), group);
		}
		
		public Collection<PermissionGroup> values() {
			return idCache.values();
		}
		
	}

}
