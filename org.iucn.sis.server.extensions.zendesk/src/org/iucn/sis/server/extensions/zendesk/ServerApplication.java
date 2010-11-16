package org.iucn.sis.server.extensions.zendesk;

import java.util.ArrayList;
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
		final ArrayList<String> keys = new ArrayList<String>();
		keys.add("org.iucn.sis.server.extension.zendesk.url");
		keys.add("org.iucn.sis.server.extension.zendesk.user");
		keys.add("org.iucn.sis.server.extension.zendesk.password");
		
		return keys;
	}

}
