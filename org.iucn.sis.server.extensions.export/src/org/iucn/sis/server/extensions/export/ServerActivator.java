package org.iucn.sis.server.extensions.export;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS Access Export";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Access Export";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ExportApplication();
	}
	
}
