package org.iucn.sis.server.extensions.demimport;

import java.util.ArrayList;
import java.util.Iterator;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.restlets.ServiceRestlet;

public class ServerApplication extends SISApplication{
	
	public ServerApplication() {
		super();
	}
	
	@Override
	public void init() {
		initServiceRoutes();	
	}
	
	protected void initServiceRoutes() {
		addResource(DEMSubmitResource.class, "/demimport", true, true, false);
		System.out.println("Init'd dem import");
	}
	
	protected void initRoutes() {
	}
	
}
