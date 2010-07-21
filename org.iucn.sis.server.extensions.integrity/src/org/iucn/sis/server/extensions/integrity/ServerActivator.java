package org.iucn.sis.server.extensions.integrity;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS Integerity Checker";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Integerity Checker";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
