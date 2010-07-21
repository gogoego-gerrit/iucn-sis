package org.iucn.sis.shared.conversions;

import org.iucn.sis.server.api.application.SISApplication;

public class Application extends SISApplication {

	@Override
	public void init() {
		addResource(new ConverterResource(), "", true, true, true);		
	}
	
	
	
}
