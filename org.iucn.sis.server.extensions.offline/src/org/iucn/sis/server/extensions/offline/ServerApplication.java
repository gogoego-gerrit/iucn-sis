package org.iucn.sis.server.extensions.offline;

import java.util.Arrays;
import java.util.Collection;

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
		addServiceToRouter(new OfflineRestlet(app.getContext()));
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		return Arrays.asList(OfflineSettings.ALL);
	}
	
}
