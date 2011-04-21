package org.iucn.sis.server.extensions.migration;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends SISActivator {

	@Override
	protected String getAppDescription() {
		return "Reports any errors during data migration from SIS 1.x to SIS 2.0";
	}
	
	@Override
	protected String getAppName() {
		return "SIS 2.0 Migration";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new Application();
	}
	
}
