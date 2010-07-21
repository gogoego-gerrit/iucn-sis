package org.iucn.sis.client.panels.users;

import org.iucn.sis.client.api.models.ClientUser;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class UserModelData extends BaseModelData implements Comparable<UserModelData> {
	private static final long serialVersionUID = 1L;

	protected final ClientUser user;
	
	public UserModelData(ClientUser user) {
		this.user = user;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> X get(String property) {
		String prop = user.getProperty(property);
		if (prop == null)
			return super.get(property);
		return (X) prop;
	}
	
	public <X extends Object> X set(String property, X value) {
		user.setProperty(property, (String) value);
		return get(property);
	};
	
	@Override
	public int compareTo(UserModelData o) {
		return user.getUsername().compareTo(o.user.getUsername());
	}
	


}
