package org.iucn.sis.server.extensions.recentasms;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS Recent Assessments";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Recent Assessments";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
