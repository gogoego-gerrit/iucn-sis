package org.iucn.sis.server.extensions.updates;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends SISActivator {

	@Override
	protected String getAppDescription() {
		return "SIS Software Updates";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Software Updates";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new Application();
	}

}
