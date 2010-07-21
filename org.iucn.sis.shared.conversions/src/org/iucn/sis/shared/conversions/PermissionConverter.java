package org.iucn.sis.shared.conversions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.HibernateException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.acl.base.PermissionGroup;
import org.iucn.sis.shared.acl.base.PermissionParser;
import org.iucn.sis.shared.acl.base.PermissionResource;
import org.iucn.sis.shared.api.models.Permission;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;

public class PermissionConverter {
	
	public static void convertAllPermissions() throws IOException, HibernateException, PersistentException {
		
		File file = new File(GoGoEgo.getInitProperties().get("sis_vfs") + "/HEAD/acl/groups.xml");
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(FileListing.readFileAsString(file));
		
		PermissionParser parser = new PermissionParser(ndoc);
		Map<String, org.iucn.sis.shared.api.models.PermissionGroup> nameToNewGroup = new HashMap<String,  org.iucn.sis.shared.api.models.PermissionGroup>();
		List<PermissionGroup> groupsNotYetParsed = new ArrayList<PermissionGroup>();
		groupsNotYetParsed.addAll(parser.getGroups().values());
		
		
		for (PermissionGroup oldPermGroup : groupsNotYetParsed) {
			nameToNewGroup.put(oldPermGroup.getName(), fromOldToNewPermissionGroup(oldPermGroup));
		}
		
		for (PermissionGroup oldGroup : groupsNotYetParsed) {
			if (oldGroup.getInherits().size() > 0)
				nameToNewGroup.get(oldGroup.getName()).setParent(nameToNewGroup.get(oldGroup.getInherits().get(0).getName()));
		}
		
		//SAVE ALL PERMISSIONS
		for (org.iucn.sis.shared.api.models.PermissionGroup group : nameToNewGroup.values())
			SIS.get().getManager().getSession().save(group);
	
		
	}
	
	private static org.iucn.sis.shared.api.models.PermissionGroup fromOldToNewPermissionGroup(PermissionGroup oldGroup) {
			org.iucn.sis.shared.api.models.PermissionGroup newGroup = new org.iucn.sis.shared.api.models.PermissionGroup();
			
			
			newGroup.setName(oldGroup.getName());
			
			for (PermissionResource oldResource : oldGroup.getResources().values()) {
				Permission newPermission = new Permission();
				
				//SET PERMISSIONS
				Boolean allow = oldResource.getSet().check(AuthorizableObject.READ);
				newPermission.setRead(allow == null ? false : allow);
				allow = oldResource.getSet().check(AuthorizableObject.WRITE);
				newPermission.setWrite(allow == null ? false : allow);
				allow = oldResource.getSet().check(AuthorizableObject.CREATE);
				newPermission.setCreate(allow == null ? false : allow);
				allow = oldResource.getSet().check(AuthorizableObject.DELETE);
				newPermission.setDelete(allow == null ? false : allow);
				allow = oldResource.getSet().check(AuthorizableObject.GRANT);
				newPermission.setGrant(allow == null ? false : allow);
				allow = oldResource.getSet().check(AuthorizableObject.USE_FEATURE);
				newPermission.setUse(allow == null ? false : allow);
				
				//SET RESOURCES
				newPermission.setUrl(oldResource.getURI());
				newPermission.setType(oldResource.getURI().split("\\Q/\\E")[0]);
				newPermission.setPermissionGroup(newGroup);
				newGroup.getPermissions().add(newPermission);
			}
			
			return newGroup;
		
	}

}
