package org.iucn.sis.server.extensions.viruses;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator {

	protected SISApplication getInstance() {
		return new ServerApplication();
	}

	@Override
	protected String getAppDescription() {
		return "SIS Virus Tracking";
	}

	@Override
	protected String getAppName() {
		return "SIS Virus Tracking";
	}

}
