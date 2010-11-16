package org.iucn.sis.server.extensions.notes;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public ServerApplication() {
		super();
	}
	
	/**
	 * Notes available online & offline
	 */
	public void init() {
		addServiceToRouter(new NotesRestlet(SIS.get().getVfsroot(), app.getContext()));
	}

}
