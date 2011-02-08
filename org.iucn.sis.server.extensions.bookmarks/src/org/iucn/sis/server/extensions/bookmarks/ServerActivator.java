package org.iucn.sis.server.extensions.bookmarks;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

/**
 * The activator class controls the plug-in life cycle
 */
public class ServerActivator extends SISActivator {

	@Override
	protected String getAppDescription() {
		return "Set bookmarks to locations within the application to easily navigate back to your favorite places.";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Bookmarks";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}

}
