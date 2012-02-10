package org.iucn.sis.server.api.utils;

public class SISGlobalSettings {
	
	public static final String SCHEMA = "org.iucn.sis.server.schema";
	public static final String VFS = "org.iucn.sis.server.vfs";
	
	public static final String CONFIG_URI = "org.iucn.sis.server.configuration.uri";
	public static final String GENERATOR = "org.iucn.sis.server.configuration.generator";
	
	public static final String ONLINE = "org.iucn.sis.server.online";
	public static final String FORCE_HTTPS = "org.iucn.sis.server.forcehttps";
	
	public static final String DB_URI = "dbsession.sis.uri";
	public static final String DB_DRIVER = "dbsession.sis.driver";
	public static final String DB_USER = "dbsession.sis.user";
	public static final String DB_PASSWORD = "dbsession.sis.password";
	public static final String DB_DIALECT = "database_dialect";
	
	public static final String LOOKUPS_URI = "dbsession.sis_lookups.uri";
	public static final String LOOKUPS_DRIVER = "dbsession.sis_lookups.driver";
	public static final String LOOKUPS_USER = "dbsession.sis_lookups.user";
	public static final String LOOKUPS_PASSWORD = "dbsession.sis_lookups.password";
	
	public static final String[] ALL = {
		SCHEMA, VFS, CONFIG_URI, GENERATOR, ONLINE, FORCE_HTTPS,
		DB_URI, DB_DRIVER, DB_USER, DB_PASSWORD, DB_DIALECT,
		LOOKUPS_URI, LOOKUPS_DRIVER, LOOKUPS_USER, LOOKUPS_PASSWORD
	};
}
