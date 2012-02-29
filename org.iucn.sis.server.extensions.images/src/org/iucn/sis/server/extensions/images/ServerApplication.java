package org.iucn.sis.server.extensions.images;

import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;

public class ServerApplication extends SISApplication {
	
	public ServerApplication() {
		super();
	}
	
	@Override
	protected void initOffline() {
		addServiceToRouter(new ImageViewerRestlet(app.getContext()), true);
	}
	
	/**
	 * Images available online & offline
	 */
	@Override
	protected void initOnline() {
		addServiceToRouter(new ImageViewerRestlet(app.getContext()), true);
		addServiceToRouter(new ImageRestlet(app.getContext()));
	}
	
	protected void addServiceToRouter(BaseServiceRestlet curService) {
		addServiceToRouter(curService, false);
	}
	
	protected void addServiceToRouter(BaseServiceRestlet curService, boolean bypassAuth) {
		addResource(curService, curService.getPaths(), bypassAuth);
	}

}
