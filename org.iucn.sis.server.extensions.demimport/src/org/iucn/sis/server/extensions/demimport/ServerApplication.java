package org.iucn.sis.server.extensions.demimport;

import java.util.Arrays;
import java.util.Collection;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	/**
	 * Import available online & offline
	 */
	public void init() {
		addResource(DEMSubmitResource.class, DEMSubmitResource.getPaths(), false);
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		return Arrays.asList(DEMSettings.ALL);
	}
	
}
