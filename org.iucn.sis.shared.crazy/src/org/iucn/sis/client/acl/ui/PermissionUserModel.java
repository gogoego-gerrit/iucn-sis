package org.iucn.sis.client.acl.ui;

import org.iucn.sis.shared.acl.User;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class PermissionUserModel extends BaseModelData implements
		Comparable<PermissionUserModel> {

	private static final long serialVersionUID = 1L;

	private User user;

	public PermissionUserModel(User user, String permissionType, boolean isAssessor) {
		this.user = user;
		set("name", user.getDisplayableName());
		set("permission", permissionType);
		set("id", user.id);
		set("assessor", Boolean.valueOf(isAssessor));
	}

	public boolean isAssessor() {
		Boolean val = get("assessor");
		return val == null ? false : val.booleanValue();
	}
	
	public int compareTo(PermissionUserModel o) {
		return ((String) get("permission")).compareTo((String) o.get("name"));
	}

	public String getName() {
		return get("name");
	}

	public String getPermission() {
		return get("permission");
	}

	public User getUser() {
		return user;
	}

}
