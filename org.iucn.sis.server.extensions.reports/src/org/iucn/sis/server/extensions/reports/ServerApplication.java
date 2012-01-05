package org.iucn.sis.server.extensions.reports;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * Reports online & offline
	 */
	public void init() {
		addServiceToRouter(new AssessmentReportRestlet(app.getContext()));
		addResource(LocalFileResource.class, LocalFileResource.getPaths(), true);
	}
	
}
