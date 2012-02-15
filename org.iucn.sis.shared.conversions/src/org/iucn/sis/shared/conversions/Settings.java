package org.iucn.sis.shared.conversions;

public class Settings {
	
	public static final String MAIL_ACCOUNT = "org.iucn.sis.conversions.mail.account"; 
	public static final String MAIL_PASSWORD = "org.iucn.sis.conversions.mail.password"; 
	public static final String MAIL_RECIPIENT = "org.iucn.sis.conversions.mail.recipient";
	
	public static final String NEW_VFS_PATH = "org.iucn.sis.server.vfs.path";
	public static final String OLD_VFS = "org.iucn.sis.conversions.vfs";
	public static final String OLD_VFS_PATH = "org.iucn.sis.conversions.vfs.path";
	
	public static final String SIS1_LOOKUPS_URI = "dbsession.sis1_lookups.uri";
	public static final String SIS1_LOOKUPS_DRIVER = "dbsession.sis1_lookups.driver";
	public static final String SIS1_LOOKUPS_USER = "dbsession.sis1_lookups.user";
	public static final String SIS1_LOOKUPS_PASSWORD = "dbsession.sis1_lookups.password";
	
	public static final String SISUSERS_URI = "dbsession.sisusers.uri";
	public static final String SISUSERS_DRIVER = "dbsession.sisusers.driver";
	public static final String SISUSERS_USER = "dbsession.sisusers.user";
	public static final String SISUSERS_PASSWORD = "dbsession.sisusers.password";
	
	public static final String REFERENCES_URI = "dbsession.ref_lookup.uri";
	public static final String REFERENCES_DRIVER = "dbsession.ref_lookup.driver";
	public static final String REFERENCES_USER = "dbsession.ref_lookup.user";
	public static final String REFERENCES_PASSWORD = "dbsession.ref_lookup.password";
	
	public static final String[] ALL = {
		NEW_VFS_PATH, OLD_VFS, OLD_VFS_PATH,
		MAIL_ACCOUNT, MAIL_PASSWORD, MAIL_RECIPIENT,
		SIS1_LOOKUPS_URI, SIS1_LOOKUPS_DRIVER, SIS1_LOOKUPS_USER, SIS1_LOOKUPS_PASSWORD,
		SISUSERS_URI, SISUSERS_DRIVER, SISUSERS_USER, SISUSERS_PASSWORD,
		REFERENCES_URI, REFERENCES_DRIVER, REFERENCES_USER, REFERENCES_PASSWORD
	};

}
