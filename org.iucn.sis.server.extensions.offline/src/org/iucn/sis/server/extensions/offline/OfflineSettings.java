package org.iucn.sis.server.extensions.offline;

public final class OfflineSettings {
	
	private static final String PREFIX = "org.iucn.sis.server.extensions.offline.";
	
	public static final String DIALECT = PREFIX + "db.dialect";
	public static final String URL = PREFIX + "db.url";
	public static final String DRIVER = PREFIX + "db.driver";
	public static final String USER = PREFIX + "db.user";
	public static final String PASSWORD = PREFIX + "db.password";
	public static final String VERSION = PREFIX + "version";
	public static final String UPDATES = PREFIX + "updates";
	
	public static final String[] REQUIRED = new String[] {
		URL, USER, PASSWORD
	};
	
	public static final String[] ALL = new String[] {
		DIALECT, URL, DRIVER, USER, PASSWORD, VERSION, UPDATES
	};
}
