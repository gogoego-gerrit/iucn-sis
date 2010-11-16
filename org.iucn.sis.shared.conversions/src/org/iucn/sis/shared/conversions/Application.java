package org.iucn.sis.shared.conversions;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class Application extends SimpleSISApplication {

	@Override
	public void init() {
		addResource(new ConverterResource(), "", true);		
	}
	
	
	
}
