package org.iucn.sis.server.extensions.findreplace;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {
	
	public void init() {
		addServiceToRouter(new FindReplaceRestlet(SIS.get().getVfsroot(), app.getContext()));
	}

}
