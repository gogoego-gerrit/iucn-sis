package org.iucn.sis.server.extensions.videos;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends SISActivator {

	public Activator() {
		super();
	}
	
	@Override
	protected String getAppDescription() {
		return "Serves links to external video content for SIS";
	}
	
	@Override
	protected String getAppName() {
		return "SIS Videos";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}

}
