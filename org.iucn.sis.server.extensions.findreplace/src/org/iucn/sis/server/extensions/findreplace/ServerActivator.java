package org.iucn.sis.server.extensions.findreplace;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS Find Replace";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Find Replace";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
