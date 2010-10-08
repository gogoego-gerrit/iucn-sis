package org.iucn.sis.server.extensions.zendesk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.restlets.ServiceRestlet;

public class ServerApplication extends SISApplication {
	
	protected final ArrayList<ServiceRestlet> services;
	
	public ServerApplication() {
		super();
		services = new ArrayList<ServiceRestlet>();
	}
	
	@Override
	public void init() {
		services.add(new ZendeskResource(app.getContext()));
		
		for (Iterator<ServiceRestlet> iter = services.iterator(); iter.hasNext();)
			addServiceToRouter(iter.next());
	}
	
	private void addServiceToRouter(ServiceRestlet curService) {
		addResource(curService, curService.getPaths(), true, true, false);
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
