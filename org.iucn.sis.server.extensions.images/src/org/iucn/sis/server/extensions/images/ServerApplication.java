package org.iucn.sis.server.extensions.images;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	/**
	 * Images available online & offline
	 */
	public void init() {
		addServiceToRouter(new ImageViewerRestlet(app.getContext()), true);
		addServiceToRouter(new ImageRestlet(app.getContext()));
	}
	
	@Override
	protected void addServiceToRouter(BaseServiceRestlet curService) {
		addServiceToRouter(curService, false);
	}
	
	protected void addServiceToRouter(BaseServiceRestlet curService, boolean bypassAuth) {
		addResource(curService, curService.getPaths(), bypassAuth);
	}

}
