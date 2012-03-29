package org.iucn.sis.server.extensions.demimport;

public class DEMSettings {
	
	public static final String DEM_SOURCE_URI = "dbsession.demSource.uri";
	public static final String DEM_SOURCE_DRIVER = "dbsession.demSource.driver";
	public static final String DEM_SOURCE_USER = "dbsession.demSource.user";
	public static final String DEM_SOURCE_PASSWORD = "dbsession.demSource.password";
	
	public static final String DEM_CONVERSION_URI = "dbsession.demConversion.uri";
	public static final String DEM_CONVERSION_DRIVER = "dbsession.demConversion.driver";
	public static final String DEM_CONVERSION_USER = "dbsession.demConversion.user";
	public static final String DEM_CONVERSION_PASSWORD = "dbsession.demConversion.password";
	
	public static final String[] ALL = {
		DEM_SOURCE_URI, DEM_SOURCE_DRIVER, DEM_SOURCE_USER, DEM_SOURCE_PASSWORD, 
		DEM_CONVERSION_URI, DEM_CONVERSION_DRIVER, DEM_CONVERSION_USER, DEM_CONVERSION_PASSWORD
	};

}
