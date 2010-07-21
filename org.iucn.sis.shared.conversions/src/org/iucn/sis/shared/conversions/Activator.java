package org.iucn.sis.shared.conversions;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class Activator extends SISActivator{
	
	@Override
	protected String getAppDescription() {
		// TODO Auto-generated method stub
		return "Conversions App";
	}
	@Override
	protected String getAppName() {
		// TODO Auto-generated method stub
		return "Conversions App";
	}
	@Override
	protected SISApplication getInstance() {
		// TODO Auto-generated method stub
		return new Application();
	}
}
