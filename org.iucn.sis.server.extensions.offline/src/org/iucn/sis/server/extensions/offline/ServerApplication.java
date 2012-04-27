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
		//For the SIS application
		addServiceToRouter(new OfflineMetaDataRestlet(app.getContext()));
		
		//For the Offline Data Management Application
		addResource(new OfflineManagerResources(app.getContext()), "/manager/resources", true);
		addResource(new OfflineManagerServicesRestlet(app.getContext()), "/services/{service}", true);
		addResource(new OfflineManagerRestlet(app.getContext()), "/manager", true);
		
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		return Arrays.asList(OfflineSettings.ALL);
	}
	
}
