package org.iucn.sis.server.extensions.batchchanges;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS Batch Changer";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Batch Changer";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
