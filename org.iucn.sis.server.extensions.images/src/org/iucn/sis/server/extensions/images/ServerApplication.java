package org.iucn.sis.server.extensions.images;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	/**
	 * Images available online & offline
	 */
	public void init() {
		addServiceToRouter(new ImageViewerRestlet(app.getContext()));
		addServiceToRouter(new ImageRestlet(app.getContext()));
	}

}
