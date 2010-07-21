package org.iucn.sis.server.extensions.redlist;

import java.util.ArrayList;
import java.util.Iterator;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.restlets.ServiceRestlet;

public class ServerApplication extends SISApplication{
	
	protected final ArrayList<ServiceRestlet> services;
	
	
	public ServerApplication() {
		super();
		services = new ArrayList<ServiceRestlet>();
		
	}
	
	@Override
	public void init() {
		initServiceRoutes();
		initRoutes();		
	}
	
	protected void initServiceRoutes() {
		
		for (Iterator<ServiceRestlet> iter = services.iterator(); iter.hasNext();)
			addServiceToRouter(iter.next());
		
	}
	
	private void addServiceToRouter(ServiceRestlet curService) {
		addResource(curService, curService.getPaths(), true, true, false);
	}
	
	protected void initRoutes() {
		RedlistRestlet redlist= new RedlistRestlet(SIS.get().getVfsroot(), app.getContext());
		addResource(redlist, redlist.getPaths(), true, false, false);
		//TODO: GET COMPILED CLIENT BITS
				
		
	}
	
	
	

}
