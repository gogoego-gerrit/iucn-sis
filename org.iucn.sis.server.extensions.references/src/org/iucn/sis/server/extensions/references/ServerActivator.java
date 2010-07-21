package org.iucn.sis.server.extensions.references;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS References";
	}
	
	@Override
	protected String getAppName() {
		return "SIS References";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
