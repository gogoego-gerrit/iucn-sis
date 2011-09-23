package org.iucn.sis.client.panels.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.models.ClientUser;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class PermissionUserModel extends BaseModelData {

	private static final long serialVersionUID = 1L;

	private ClientUser user;

	public PermissionUserModel(ClientUser user, String permissionType, boolean isAssessor) {
		this.user = user;
		set("name", user.getDisplayableName());
		set("permission", new PermissionsModelData(permissionType));
		set("id", user.getId());
		set("assessor", Boolean.valueOf(isAssessor));
	}

	public boolean isAssessor() {
		Boolean val = get("assessor");
		return val == null ? false : val.booleanValue();
	}
	
	/*public int compareTo(PermissionUserModel o) {
		return ((String) get("permission")).compareTo((String) o.get("name"));
	}*/

	public String getName() {
		return get("name");
	}

	public PermissionsModelData getPermission() {
		return get("permission");
	}

	public ClientUser getUser() {
		return user;
	}
	
	public static class PermissionsModelData extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		private final Collection<String> permissions;
		
		public PermissionsModelData(String csv) {
			permissions = new HashSet<String>();
			if (csv != null) {
				for (String permission : csv.split(","))
					permissions.add(permission);
			}
			set("text", csv);
			set("value", csv);
		}
		
		public PermissionsModelData(List<String> options) {
			this(toCSV(options));
		}
		
		public boolean hasPermission(String value) {
			return permissions.contains(value);
		}
		
		public List<String> toList() {
			return new ArrayList<String>(permissions);
		}
		
		public String toCSV() {
			return toCSV(permissions);
		}
		
		private static String toCSV(Collection<String> permissions) {
			StringBuilder out = new StringBuilder();
			for (Iterator<String> iter = permissions.iterator(); iter.hasNext(); ) {
				out.append(iter.next());
				out.append(iter.hasNext() ? "," : "");
			}
			return out.toString();
		}
		
		@Override
		public String toString() {
			return get("text");
		}
		
	}

}
