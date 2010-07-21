package org.iucn.sis.server.extensions.zendesk;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS ZenDesk";
	}
	
	@Override
	protected String getAppName() {
		return "SIS ZenDesk";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
