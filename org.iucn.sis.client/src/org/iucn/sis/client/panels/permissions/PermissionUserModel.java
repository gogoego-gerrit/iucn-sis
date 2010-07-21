package org.iucn.sis.client.panels.permissions;

import org.iucn.sis.client.api.models.ClientUser;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class PermissionUserModel extends BaseModelData implements
		Comparable<PermissionUserModel> {

	private static final long serialVersionUID = 1L;

	private ClientUser user;

	public PermissionUserModel(ClientUser user, String permissionType, boolean isAssessor) {
		this.user = user;
		set("name", user.getDisplayableName());
		set("permission", permissionType);
		set("id", user.getId());
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

	public ClientUser getUser() {
		return user;
	}

}
