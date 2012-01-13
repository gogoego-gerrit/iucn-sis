package org.iucn.sis.server.extensions.zendesk;

import java.util.Arrays;
import java.util.Collection;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	/**
	 * Requires hitting ZenDesk API, only available online.
	 */
	public void init() {
		addServiceToRouter(new ZendeskResource(app.getContext()));
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		return Arrays.asList(Settings.ALL);
	}

}
