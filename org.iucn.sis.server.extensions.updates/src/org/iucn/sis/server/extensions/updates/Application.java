package org.iucn.sis.server.extensions.updates;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.extensions.updates.restlets.SoftwareVersionRestlet;

public class Application extends SimpleSISApplication {

	public Application() {
		super(RunMode.ONLINE);
	}

	@Override
	public void init() {
		SoftwareVersionRestlet service = new SoftwareVersionRestlet(app.getContext());
		addResource(service, service.getPaths(), true);
	}

}
