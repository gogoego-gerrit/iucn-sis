package org.iucn.sis.server.extensions.fieldmanager;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends SISActivator {

	@Override
	protected String getAppDescription() {
		return "SIS Field Manager";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Field Manager";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}

}
