package org.iucn.sis.shared.conversions;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class Activator extends SISActivator{
	
	@Override
	protected String getAppDescription() {
		return "SIS 1.0 to 2.0 Conversion";
	}
	@Override
	protected String getAppName() {
		return "SIS 1.0 to 2.0 Conversion";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new Application();
	}
}
