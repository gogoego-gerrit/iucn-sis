package org.iucn.sis.server.extensions.tags;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * Tagging available online & offline
	 */
	public void init() {
		addServiceToRouter(new MarkedRestlet(SIS.get().getVfsroot(), app.getContext()));
	}

}
