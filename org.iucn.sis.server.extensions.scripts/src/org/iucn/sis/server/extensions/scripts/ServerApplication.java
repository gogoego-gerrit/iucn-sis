package org.iucn.sis.server.extensions.scripts;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * For internal use only -- hopefully never used.
	 */
	public ServerApplication() {
		super(RunMode.ONLINE);
	}

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
		}, "/start", false);
		
	}

}
