package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.models.ClientUser;

public class UserCache {

	protected List<ClientUser> users;
	protected Map<Integer, ClientUser> idToUsers;
	public static final UserCache impl = new UserCache();

	protected UserCache() {
		idToUsers = new HashMap<Integer, ClientUser>();
		users = new ArrayList<ClientUser>() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean add(ClientUser e) {
				remove(e);
				return super.add(e);
			}

			@Override
			public boolean addAll(Collection<? extends ClientUser> c) {
				for (ClientUser user : c) {
					add(user);
				}
				return true;
			}

			@Override
			public boolean contains(Object o) {
				int id = ((ClientUser) o).getId();
				for (ClientUser user : users) {
					if (user.getId() == id)
						return true;
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				int id = ((ClientUser) o).getId();
				for (ClientUser user : users) {
					if (user.getId() == id) {
						super.remove(user);
						return true;
					}

				}
				return false;
			}
		};
	}

	public void addUser(ClientUser user) {
		users.add(user);
		idToUsers.put(user.getId(), user);
	}

	public void addUsers(Collection<ClientUser> users) {
		this.users.addAll(users);
		for( ClientUser user : users )
			idToUsers.put(user.getId(), user);
	}

	public String generateTextFromUserIDs(List<Integer> ids) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < ids.size(); i++) {
			text.append(idToUsers.get(ids.get(i)).getCitationName());
			
			if (i + 1 < ids.size() - 1)
				text.append(", ");

			else if (i + 1 == ids.size() - 1)
				text.append(" & ");
		}
		return text.toString();
	}
	
	public List<ClientUser> getUsers() {
		List<ClientUser> newList = new ArrayList<ClientUser>(users.size());
		for (ClientUser user : users)
			newList.add(user);
		return newList;
	}

	public void removeUser(ClientUser user) {
		this.users.remove(user);
		idToUsers.remove(user.getId());
	}
}
