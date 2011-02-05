package org.iucn.sis.server.extensions.recentasms;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super();
	}
	
	/**
	 * Recent assessments both online & offline
	 */
	public void init() {
		addServiceToRouter(new RecentlyAccessedRestlet(app.getContext()));
	}
	

}
