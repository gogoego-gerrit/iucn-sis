package org.iucn.sis.server.extensions.user.application;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS User Application";
	}
	
	@Override
	protected String getAppName() {
		return "SIS User Application";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new UserManagementApplication();
	}
	
}
