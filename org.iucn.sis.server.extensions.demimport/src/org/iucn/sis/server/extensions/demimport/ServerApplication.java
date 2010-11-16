package org.iucn.sis.server.extensions.demimport;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	/**
	 * Import available online & offline
	 */
	public void init() {
		addResource(DEMSubmitResource.class, "/demimport", false);
	}
		
}
