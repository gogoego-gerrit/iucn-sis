package org.iucn.sis.server.extensions.definitions;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * Definitions available online & offline
	 */
	public void init() {
		addServiceToRouter(new DefinitionsRestlet(SIS.get().getVfsroot(), app.getContext()));
	}

}
