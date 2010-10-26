package org.iucn.sis.server.extensions.scripts;

import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class ServerApplication extends SISApplication{

	@Override
	public void init() {
		
		//ADD WHATEVER SCRIPT THAT YOU WANT RUN HERE
		addResource( new ServiceRestlet(app.getContext()) {
			
			@Override
			public void performService(Request request, Response response) {
				// TODO Call your script here!
				
			}
			
			@Override
			public void definePaths() {
				
				
			}
		}, "", true, false, true);
		
	}

}
