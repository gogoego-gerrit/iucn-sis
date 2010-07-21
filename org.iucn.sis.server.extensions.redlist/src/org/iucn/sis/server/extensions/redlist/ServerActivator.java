package org.iucn.sis.server.extensions.redlist;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS Redlist App";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Redlist App";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
