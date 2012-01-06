package org.iucn.sis.client.api.caches;

import java.util.List;

import org.iucn.sis.client.api.caches.RecentlyAccessedCache.RecentUser;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.ui.models.users.UserModelData;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.models.User;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class UserStore {
	
	public static final UserStore impl = new UserStore();
	
	private final ListStore<UserModelData> active, disabled;
	
	private boolean loaded = false;
	
	private UserStore() {
		active = new ListStore<UserModelData>();
		disabled = new ListStore<UserModelData>();
	}
	
	public void load(final SimpleListener callback) {
		if (loaded) {
			callback.handleEvent();
			return;
		}
		
		if (!AuthorizationCache.impl.canUse(AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
			active.add(new UserModelData(SISClientBase.currentUser));
			loaded = true;
			callback.handleEvent();
		} else {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.get(UriBase.getInstance().getUserBase() + "/users?sort=username", new GenericCallback<String>() {
				public void onSuccess(String result) {
					IncrementalUserParser parser = new IncrementalUserParser(ndoc, active, disabled);
					parser.setListener(new SimpleListener() {
						public void handleEvent() {
							WindowUtils.hideLoadingAlert();
							
							loaded = true;
							callback.handleEvent();
						}
					});
					
					WindowUtils.showLoadingAlert("Loading Users...");
					
					DeferredCommand.addPause();
					DeferredCommand.addCommand(parser);
				}
	
				@Override
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Unable to load users");
	
				}
			});
		}
	}
	
	public void doLogout() {
		loaded = false;
		active.removeAll();
		disabled.removeAll();
	}
	
	public ListStore<UserModelData> getActiveUsers() {
		return active;
	}
	
	public ListStore<UserModelData> getDisabledUsers() {
		return disabled;
	}
	
	public void disableUser(UserModelData user) {
		/*
		 * TODO: call the save function to disable the user, 
		 * then remove the from the active user store and 
		 * add them to the inactive user store
		 */
		active.remove(user);
		disabled.add(user);
		
		final GenericCallback<Object> callback = new GenericCallback<Object>() {
			public void onSuccess(Object result) { }
			public void onFailure(Throwable caught) { }
		};
	
		List<RecentUser> list = 
			RecentlyAccessedCache.impl.list(RecentlyAccessed.USER);
		for (RecentUser cached : list) {
			if (cached.getUser().getId() == Integer.parseInt((String)user.get("id")))
				RecentlyAccessedCache.impl.delete(cached, callback);
		}
		
		user.set(ClientUser.STATE, Integer.toString(ClientUser.DELETED));
	}
	
	public void activateUser(UserModelData user) {
		/*
		 * TODO: call the save function to activate the user, 
		 * then remove the from the disabled user store and 
		 * add them to the active user store
		 */
		disabled.remove(user);
		active.add(user);
		
		user.set(ClientUser.STATE, Integer.toString(ClientUser.ACTIVE));
	}
	
	public void addUser(String username) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getUserBase() + "/users/" + username, new GenericCallback<String>() {
			public void onSuccess(String result) {
				NativeElement node = ndoc.getDocumentElement().getElementByTagName(User.ROOT_TAG);
				active.add(new UserModelData(ClientUser.fromXML(node)));
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error",
						"Failed to load new user.  If you want to view the new user, please reopen window.");
			}
		});
	}
	
	public void removeUser(UserModelData user, ListStore<UserModelData> store) {
		store.remove(user);
	}
	
	public static class IncrementalUserParser implements IncrementalCommand {
		
		private static final int NUM_TO_PARSE = 200;
		private static final int DOWNLOAD_LIMIT = 5000;
		
		private final NativeNodeList nodes;
		private final ListStore<UserModelData> active, disabled;
		
		private int current = 0;
		private int size;
		
		private SimpleListener listener;
		
		public IncrementalUserParser(NativeDocument document, ListStore<UserModelData> active, ListStore<UserModelData> disabled) {
			this.active = active;
			this.disabled = disabled;
			this.nodes = document.getDocumentElement().getElementsByTagName(User.ROOT_TAG);
			this.size = nodes.getLength();
		}
		
		@Override
		public boolean execute() {
			if (current >= size || current >= DOWNLOAD_LIMIT) {
				/*ArrayUtils.quicksort(loader.getFullList(), new Comparator<UserModelData>() {
					public int compare(UserModelData o1, UserModelData o2) {
						return ((String) o1.get("username")).compareTo((String) o2.get("username"));
					}
				});*/
				//loader.getPagingLoader().load();
				
				WindowUtils.hideLoadingAlert();
				
				if (listener != null)
					listener.handleEvent();
				
				return false;
			}
			
			int max = current + NUM_TO_PARSE;
			if (max > size)
				max = size;
			
			WindowUtils.showLoadingAlert("Loading Users " + (current+1) + "-" + (max) + " of " + size);
			
			for (int i = current; i < current + NUM_TO_PARSE && i < size; i++) {
				ClientUser user = ClientUser.fromXML(nodes.elementAt(i));
				if (user.getState() == ClientUser.ACTIVE)
					active.add(new UserModelData(user));
				else
					disabled.add(new UserModelData(user));
			}
			
			current += NUM_TO_PARSE;
			
			return true;
		}
		
		public void setListener(SimpleListener listener) {
			this.listener = listener;
		}
		
	}

}
