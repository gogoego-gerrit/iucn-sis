package org.iucn.sis.server.extensions.batchchanges;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	/**
	 * Batch change is available both on & offline 
	 */
	public void init() {
		addServiceToRouter(new BatchChangeRestlet(app.getContext()));
	}
	
}
