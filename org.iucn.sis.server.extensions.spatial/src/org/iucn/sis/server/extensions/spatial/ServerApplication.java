package org.iucn.sis.server.extensions.spatial;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public void init() {
		addServiceToRouter(new SpatialInformationRestlet(app.getContext()));
	}
	
}
