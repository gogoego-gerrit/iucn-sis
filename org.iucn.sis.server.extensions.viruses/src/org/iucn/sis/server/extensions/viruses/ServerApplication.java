package org.iucn.sis.server.extensions.viruses;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.application.SISApplication;

public class ServerApplication extends SISApplication {

	public void init() {
		final List<String> paths = new ArrayList<String>();
		paths.add("/viruses");
		paths.add("/viruses/{identifier}");
		
		addResource(VirusResource.class, paths, true, true, false);
	}

}
