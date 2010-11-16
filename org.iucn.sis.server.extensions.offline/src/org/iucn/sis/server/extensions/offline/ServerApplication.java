package org.iucn.sis.server.extensions.offline;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * Offline only runs -- offline :)
	 */
	public ServerApplication() {
		super(RunMode.OFFLINE);
	}
	
	@Override
	public void init() {
		addServiceToRouter(new OfflineRestlet(SIS.get().getVfsroot(), app.getContext()));
	}
	
}
