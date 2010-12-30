package org.iucn.sis.server.extensions.redlist;

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

}
