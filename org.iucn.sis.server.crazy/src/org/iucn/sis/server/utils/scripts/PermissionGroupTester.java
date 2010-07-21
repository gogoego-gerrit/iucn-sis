package org.iucn.sis.server.utils.scripts;

import java.io.IOException;
import java.util.Arrays;

import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.shared.acl.base.PermissionGroup;
import org.iucn.sis.shared.acl.base.PermissionParser;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class PermissionGroupTester {

	public static void testPermissions(VFS vfs) throws IOException {
		PermissionParser parser;
		
		final NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.get("/acl/groups", new GenericCallback<String>() {
			public void onSuccess(String result) {}
			public void onFailure(Throwable caught) {}
		});
		
		parser = new PermissionParser(ndoc);
		
		VFSPath users = new VFSPath("/users");
		VFSPathToken [] tokens = vfs.list(users);
	
		for( VFSPathToken token : tokens ) {
			VFSPath cur = users.child(token);
			
			if( vfs.isCollection(cur) ) {
				VFSPath profilePath = new VFSPath(cur.toString() + "/profile.xml");
				if( vfs.exists(profilePath)) {
					String profile = vfs.getString(profilePath);
					if( profile.indexOf("<sis>") < 0 ) {
						profile = profile.replace("</profile>", "<sis>true</sis>\n</profile>");
						DocumentUtils.writeVFSFile(profilePath.toString(), vfs, profile);
					}
					
					if( profile.indexOf("<quickGroup>") > 0 ) {
						String quickGroup = profile.substring(profile.indexOf("<quickGroup>"), profile.indexOf("</quickGroup>")).replace("<quickGroup>", "");
						String newGroup = determineNewGroup(quickGroup);
						String groupToUse = newGroup.equals("") ? quickGroup : newGroup;
						
						String [] userGroups;
						if( groupToUse.indexOf(",") > -1 )
							userGroups = groupToUse.split(",");
						else
							userGroups = new String[] { groupToUse };

						for( String groupName : Arrays.asList(userGroups) ) {
							PermissionGroup curGroup = parser.getGroup(groupName.replaceAll("'", ""));
							if( curGroup == null ) {
								System.out.println("Unable to find group " + groupName.replaceAll("'", "") + " for user " + cur.toString());								
							} else {
								//It's a valid group
								profile = profile.replace(quickGroup, newGroup);
								DocumentUtils.writeVFSFile(profilePath.toString(), vfs, profile);
							}
						}
					} else {
						System.out.println("User " + cur.toString() + " is missing quickGroup.");
					}
				} else 
					System.out.println("Path " + cur.toString() + " is not a valid user.");
			}
		}
	}
	
	private static String determineNewGroup(String curGroup) {
		String newGroup = "";
		if( curGroup == null )
			newGroup = "sysAdmin";
		else if( curGroup.equals("'guest'") )
			newGroup = "guest";
		else if( curGroup.equals("'workingSet','no_taxomatic'") )
			newGroup = "workingSetAssessor";
		else if( curGroup.equals("'workingSet','no_taxomatic','can_batch','canCreateDraft','reference_replacer'") )
			newGroup = "workingSetFacilitator,batchChangeUser,referenceReplaceUser";
		else if( curGroup.equals("'molluscs','no_taxomatic'") )
			newGroup = "molluscsAssessor";
		else if( curGroup.contains("'bryophyta','no_taxomatic'") )
			newGroup = "bryophytaFacilitator";
		else if( curGroup.equals("'cephalopods','workingSet','no_taxomatic'") )
			newGroup = "cephalopodAssessor,workingSetAssessor";
		else if( curGroup.equals("'Lepidoptera','workingSet','no_taxomatic'") )
			newGroup = "lepidopteraAssessor,workingSetAssessor";
		else if( curGroup.equals("'Lepidoptera','no_taxomatic'") )
			newGroup = "lepidopteraAssessor";
		else if( curGroup.equals("'gaa','gma'") )
			newGroup = "gaaAssessor,gmaAssessor,taxomaticUser";
		else if( curGroup.equals("'gaa','gma','canEditPublished'") )
			newGroup = "gaaAdmin,gmaAdmin,taxomaticUser";
		else if( curGroup.equals("'gaa','gma','canEditPublished','no_taxomatic'") )
			newGroup = "gaaAdmin,gmaAdmin";
		else if( curGroup.contains("'gaa','canCreateDraft','canEditPublished'") )
			newGroup = "gaaAdmin,gmaAdmin";
		else if( curGroup.equals("'rlu','workingSet'"))
			newGroup = "rlu";
		else if( curGroup.equals("'rlu','no_taxomatic'"))
			newGroup = "rlu,redactTaxomatic";
		else if( curGroup.equals("'gma','canEditPublished','workingSet'") )
			newGroup = "gmaAdmin,workingSetAdmin";
		else if( curGroup.contains("'workingSet'"))
			newGroup = "workingSetAssessor";
		else if( curGroup.startsWith("'gaa','reptiles','canEditPublished','canCreateDraft','canDeleteDraft'"))
			newGroup = "gaaAdmin,reptilesAdmin";
		else if( curGroup.equals("'reptiles'"))
			newGroup = "reptilesAssessor";
		else if( curGroup.equals("'Cambaridae','workingSet','can_batch','canCreateDraft'"))
			newGroup = "cambaridaeFacilitator,workingSetFacilitator,batchChangeUser";
		else if( curGroup.equals("'Cambaridae','workingSet','canCreateDraft'"))
			newGroup = "cambaridaeFacilitator,workingSetFacilitator";
		
		if( curGroup.contains("can_batch") )
			newGroup += ",batchChangeUser";
		if( curGroup.contains("reference_replacer") )
			newGroup += ",referenceReplaceUser";
		if( curGroup.contains("find_replace") )
			newGroup += ",findReplaceUser";
		if( curGroup.contains("no_taxomatic") )
			newGroup += ",redactTaxomatic";
		
		return newGroup;
	}
}
