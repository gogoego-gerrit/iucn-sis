package org.iucn.sis.shared.api.acl.feature;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;

import com.solertium.util.portable.PortableAlphanumericComparator;

public class AuthorizableFeature implements AuthorizableObject, Comparable<AuthorizableFeature> {
	
	private static final String TAXOMATIC = "taxomatic";
	private static final String BATCH_CHANGE = "batchChange";
	private static final String FIND_REPLACE = "findReplace";
	private static final String DEM_UPLOAD = "demUpload";
	private static final String TAXON_FINDER = "taxonFinder";
	private static final String EDIT_REGIONS = "editRegions";
	private static final String PERMISSION_MANAGEMENT = "permissionManagement";
	private static final String ACCESS_EXPORT = "accessExport";
	private static final String WORKING_SET_ACCESS_EXPORT = "workingSetAccessExport";
	private static final String USER_MANAGEMENT = "userManagement";
	private static final String DEFINITION_MANAGEMENT = "definitionManagement";
	private static final String DELETE_USERS = "deleteUsers";
	private static final String INTEGRITY_CHECK = "integrityCheck";
	private static final String LOCK_MANAGEMENT = "assessmentLockManagement"; 
	private static final String BATCH_UPLOAD = "batchUpload";
	private static final String REDLIST = "redlist";
	private static final String VIRUS_MANAGEMENT = "virusManagement";
	private static final String TAXA_TAGGING = "taxaTagging";
	private static final String ADD_PROFILE = "addProfile";
	private static final String PUBLICATION_MANAGER = "publicationManager";
	private static final String PUBLICATION_MANAGER_EDIT = "publicationManagerEdit";
	
	public static AuthorizableFeature TAXOMATIC_FEATURE = new AuthorizableFeature(TAXOMATIC, "Taxomatic"); 
	public static AuthorizableFeature BATCH_CHANGE_FEATURE = new AuthorizableFeature(BATCH_CHANGE, "Batch Change");
	public static AuthorizableFeature FIND_REPLACE_FEATURE = new AuthorizableFeature(FIND_REPLACE, "Find/Replace");
	public static AuthorizableFeature DEM_UPLOAD_FEATURE = new AuthorizableFeature(DEM_UPLOAD, "DEM Upload");
	public static AuthorizableFeature TAXON_FINDER_FEATURE = new AuthorizableFeature(TAXON_FINDER, "Manage New Taxa");
	public static AuthorizableFeature EDIT_REGIONS_FEATURE = new AuthorizableFeature(EDIT_REGIONS, "Edit Regions");
	public static AuthorizableFeature PERMISSION_MANAGEMENT_FEATURE = new AuthorizableFeature(PERMISSION_MANAGEMENT, "Permission Management");
	public static AuthorizableFeature ACCESS_EXPORT_FEATURE = new AuthorizableFeature(ACCESS_EXPORT, "Access Export");
	public static AuthorizableFeature WORKING_SET_ACCESS_EXPORT_FEATURE = new AuthorizableFeature(WORKING_SET_ACCESS_EXPORT, "Working Set Access Export");
	public static AuthorizableFeature USER_MANAGEMENT_FEATURE = new AuthorizableFeature(USER_MANAGEMENT, "User Management");
	public static AuthorizableFeature DEFINITION_MANAGEMENT_FEATURE = new AuthorizableFeature(DEFINITION_MANAGEMENT, "Definition Management");
	public static AuthorizableFeature DELETE_USERS_FEATURE = new AuthorizableFeature(DELETE_USERS, "Delete Users");
	public static AuthorizableFeature INTEGRITY_CHECK_FEATURE = new AuthorizableFeature(INTEGRITY_CHECK, "Integrity Check Management");
	public static AuthorizableFeature LOCK_MANAGEMENT_FEATURE = new AuthorizableFeature(LOCK_MANAGEMENT, "Lock Management");
	public static AuthorizableFeature BATCH_UPLOAD_FEATURE = new AuthorizableFeature(BATCH_UPLOAD, "Batch Image Upload");
	public static AuthorizableFeature REDLIST_FEATURE = new AuthorizableFeature(REDLIST, "RedList Image Publish");
	public static AuthorizableFeature VIRUS_MANAGEMENT_FEATURE = new AuthorizableFeature(VIRUS_MANAGEMENT, "Virus Management");
	public static AuthorizableFeature TAXA_TAGGING_FEATURE = new AuthorizableFeature(TAXA_TAGGING, "Taxon Tag Management");
	public static AuthorizableFeature ADD_PROFILE_FEATURE = new AuthorizableFeature(ADD_PROFILE, "Add User Profiles");
	public static AuthorizableFeature PUBLICATION_MANAGER_FEATURE = new AuthorizableFeature(PUBLICATION_MANAGER, "View Publication Workflow");
	public static AuthorizableFeature PUBLICATION_MANAGER_EDITING_FEATURE = new AuthorizableFeature(PUBLICATION_MANAGER_EDIT, "Edit Publication Workflow");
	
	public static String [] featureNames = new String [] { TAXOMATIC, BATCH_CHANGE, 
		FIND_REPLACE, DEM_UPLOAD, TAXON_FINDER, EDIT_REGIONS,  
		PERMISSION_MANAGEMENT, ACCESS_EXPORT, WORKING_SET_ACCESS_EXPORT, USER_MANAGEMENT, 
		DEFINITION_MANAGEMENT, DELETE_USERS, INTEGRITY_CHECK, VIRUS_MANAGEMENT, TAXA_TAGGING,
		ADD_PROFILE, PUBLICATION_MANAGER, PUBLICATION_MANAGER_EDIT
	};
	
	public static AuthorizableFeature [] features = new AuthorizableFeature [] { TAXOMATIC_FEATURE, BATCH_CHANGE_FEATURE, 
		FIND_REPLACE_FEATURE, DEM_UPLOAD_FEATURE, TAXON_FINDER_FEATURE, EDIT_REGIONS_FEATURE, 
		PERMISSION_MANAGEMENT_FEATURE, ACCESS_EXPORT_FEATURE, 
		WORKING_SET_ACCESS_EXPORT_FEATURE, USER_MANAGEMENT_FEATURE, 
		DEFINITION_MANAGEMENT_FEATURE, DELETE_USERS_FEATURE, INTEGRITY_CHECK_FEATURE, 
		VIRUS_MANAGEMENT_FEATURE, TAXA_TAGGING_FEATURE, ADD_PROFILE_FEATURE,
		PUBLICATION_MANAGER_FEATURE, PUBLICATION_MANAGER_EDITING_FEATURE
	};
	
	
	private final String name;
	private final String description;
	
	private AuthorizableFeature(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getFullURI() {
		return AuthorizableObject.FEATURE_TYPE_PATH + "/" + name;
	}
	
	public String getProperty(String key) {
		return "";
	}
	
	@Override
	public int compareTo(AuthorizableFeature arg0) {
		return new PortableAlphanumericComparator().compare(this, arg0);
	}
	
	@Override
	public String toString() {
		return description;
	}
}
