package org.iucn.sis.shared.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.shared.acl.User;

public class UserCache {

	protected List<User> users;
	public static final UserCache impl = new UserCache();

	protected UserCache() {
		users = new ArrayList<User>() {

			@Override
			public boolean add(User e) {
				remove(e);
				return super.add(e);
			}

			@Override
			public boolean addAll(Collection<? extends User> c) {
				for (User user : c) {
					add(user);
				}
				return true;
			}

			@Override
			public boolean contains(Object o) {
				Long id = ((User) o).getId();
				for (User user : users) {
					if (user.getId() == id)
						return true;
				}
				return false;
			}

			@Override
			public boolean remove(Object o) {
				Long id = ((User) o).getId();
				for (User user : users) {
					if (user.getId() == id) {
						super.remove(user);
						return true;
					}

				}
				return false;
			}
		};
	}

	public void addUser(User user) {
		users.add(user);
	}

	public void addUsers(Collection<User> users) {
		this.users.addAll(users);
	}

	public List<User> getUsers() {
		List<User> newList = new ArrayList<User>(users.size());
		for (User user : users)
			newList.add(user);
		return newList;
	}

	public void removeUser(User user) {
		this.users.remove(user);
	}
}
