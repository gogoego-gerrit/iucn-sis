package org.iucn.sis.server.extensions.offline;

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
		
//		services.add(new OfflineRestlet(SIS.get().getVfsroot(), app.getContext()));
		
		for (Iterator<ServiceRestlet> iter = services.iterator(); iter.hasNext();)
			addServiceToRouter(iter.next());
		
	}
	
	private void addServiceToRouter(ServiceRestlet curService) {
		addResource(curService, curService.getPaths(), true, true, false);
	}
	
	protected void initRoutes() {
		OfflineRestlet offline = new OfflineRestlet(SIS.get().getVfsroot(), app.getContext());
		addResource(offline, offline.getPaths(), false, true, true);
		//TODO: GET COMPILED CLIENT BITS
				
		
	}
	
	
	

}
