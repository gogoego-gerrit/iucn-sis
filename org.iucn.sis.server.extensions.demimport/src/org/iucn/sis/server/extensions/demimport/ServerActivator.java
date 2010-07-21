package org.iucn.sis.server.extensions.demimport;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS DEM Import";
	}
	
	@Override
	protected String getAppName() {
		return "SIS DEM Import";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
