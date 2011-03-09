package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.models.ClientUser;

public class UserCache {

	public static final UserCache impl = new UserCache();
	
	private final Map<Integer, ClientUser> cache;
	
	private UserCache() {
		cache = new HashMap<Integer, ClientUser>();
	}

	public void addUser(ClientUser user) {
		cache.put(user.getId(), user);
	}

	public void addUsers(Collection<ClientUser> users) {
		for (ClientUser user : users)
			addUser(user);
	}

	public String generateTextFromUserIDs(List<Integer> ids) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < ids.size(); i++) {
			ClientUser user = cache.get(ids.get(i));
			if (user == null)
				continue;
					
			text.append(user.getCitationName());
			
			if (i + 1 < ids.size() - 1)
				text.append(", ");

			else if (i + 1 == ids.size() - 1)
				text.append(" & ");
		}
		return text.toString();
	}
	
	public List<ClientUser> getUsers() {
		return new ArrayList<ClientUser>(cache.values());
	}

	public void removeUser(ClientUser user) {
		cache.remove(user.getId());
	}
	
	public boolean hasUser(Integer userID) {
		return cache.containsKey(userID);
	}
	
	public ClientUser getUser(Integer userID) {
		return cache.get(userID);
	}
}
