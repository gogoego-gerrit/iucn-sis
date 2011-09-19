package org.iucn.sis.server.extensions.redlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * Red list connection only available online.
	 */
	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	@Override
	public void init() {
		addServiceToRouter(new RedlistRestlet(app.getContext()));
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		List<String> keys = new ArrayList<String>();
		keys.add("org.iucn.sis.server.extensions.redlist.imagePublishURL");
		return keys;
	}

}
