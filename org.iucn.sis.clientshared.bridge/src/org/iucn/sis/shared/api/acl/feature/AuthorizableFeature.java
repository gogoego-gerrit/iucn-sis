package org.iucn.sis.shared.api.acl.feature;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;

public class AuthorizableFeature implements AuthorizableObject {
	
	public static final String TAXOMATIC = "taxomatic";
	public static final String BATCH_CHANGE = "batchChange";
	public static final String FIND_REPLACE = "findReplace";
	public static final String DEM_UPLOAD = "demUpload";
	public static final String TAXON_FINDER = "taxonFinder";
	public static final String EDIT_REGIONS = "editRegions";
	public static final String REFERENCE_REPLACE = "referenceReplace";
	public static final String PERMISSION_MANAGEMENT = "permissionManagement";
	public static final String ACCESS_EXPORT = "accessExport";
	public static final String WORKING_SET_ACCESS_EXPORT = "workingSetAccessExport";
	public static final String USER_MANAGEMENT = "userManagement";
	public static final String DEFINITION_MANAGEMENT = "definitionManagement";
	public static final String DELETE_USERS = "deleteUsers";
	public static final String INTEGRITY_CHECK = "integrityCheck";
	public static final String LOCK_MANAGEMENT = "assessmentLockManagement"; 
	public static final String BATCH_UPLOAD = "batchUpload";
	public static final String REDLIST = "redlist";
	public static final String VIRUS_MANAGEMENT = "viursManagement";
	
	public static String [] featureNames = new String [] { TAXOMATIC, BATCH_CHANGE, 
		FIND_REPLACE, DEM_UPLOAD, TAXON_FINDER, EDIT_REGIONS, REFERENCE_REPLACE, 
		PERMISSION_MANAGEMENT, ACCESS_EXPORT, WORKING_SET_ACCESS_EXPORT, USER_MANAGEMENT, 
		DEFINITION_MANAGEMENT, DELETE_USERS, INTEGRITY_CHECK, VIRUS_MANAGEMENT };
	
	public static AuthorizableFeature TAXOMATIC_FEATURE = new AuthorizableFeature(TAXOMATIC); 
	public static AuthorizableFeature BATCH_CHANGE_FEATURE = new AuthorizableFeature(BATCH_CHANGE);
	public static AuthorizableFeature FIND_REPLACE_FEATURE = new AuthorizableFeature(FIND_REPLACE);
	public static AuthorizableFeature DEM_UPLOAD_FEATURE = new AuthorizableFeature(DEM_UPLOAD);
	public static AuthorizableFeature TAXON_FINDER_FEATURE = new AuthorizableFeature(TAXON_FINDER);
	public static AuthorizableFeature EDIT_REGIONS_FEATURE = new AuthorizableFeature(EDIT_REGIONS);
	public static AuthorizableFeature REFERENCE_REPLACE_FEATURE = new AuthorizableFeature(REFERENCE_REPLACE);
	public static AuthorizableFeature PERMISSION_MANAGEMENT_FEATURE = new AuthorizableFeature(PERMISSION_MANAGEMENT);
	public static AuthorizableFeature ACCESS_EXPORT_FEATURE = new AuthorizableFeature(ACCESS_EXPORT);
	public static AuthorizableFeature WORKING_SET_ACCESS_EXPORT_FEATURE = new AuthorizableFeature(WORKING_SET_ACCESS_EXPORT);
	public static AuthorizableFeature USER_MANAGEMENT_FEATURE = new AuthorizableFeature(USER_MANAGEMENT);
	public static AuthorizableFeature DEFINITION_MANAGEMENT_FEATURE = new AuthorizableFeature(DEFINITION_MANAGEMENT);
	public static AuthorizableFeature DELETE_USERS_FEATURE = new AuthorizableFeature(DELETE_USERS);
	public static AuthorizableFeature INTEGRITY_CHECK_FEATURE = new AuthorizableFeature(INTEGRITY_CHECK);
	public static AuthorizableFeature LOCK_MANAGEMENT_FEATURE = new AuthorizableFeature(LOCK_MANAGEMENT);
	public static AuthorizableFeature BATCH_UPLOAD_FEATURE = new AuthorizableFeature(BATCH_UPLOAD);
	public static AuthorizableFeature REDLIST_FEATURE = new AuthorizableFeature(REDLIST);
	public static AuthorizableFeature VIRUS_MANAGEMENT_FEATURE = new AuthorizableFeature(VIRUS_MANAGEMENT);
	
	private String featureName;
	
	private AuthorizableFeature(String featureName) {
		this.featureName = featureName;
	}
	
	public String getFullURI() {
		return AuthorizableObject.FEATURE_TYPE_PATH + "/" + featureName;
	}
	
	public String getProperty(String key) {
		return "";
	}
}
