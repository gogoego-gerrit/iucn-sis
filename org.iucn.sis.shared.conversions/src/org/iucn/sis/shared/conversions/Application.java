package org.iucn.sis.shared.conversions;

import java.util.Arrays;
import java.util.Collection;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class Application extends SimpleSISApplication {

	@Override
	public void init() {
		addResource(new ConverterResource(), "", true);		
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		return Arrays.asList(Settings.ALL);
	}
	
}
