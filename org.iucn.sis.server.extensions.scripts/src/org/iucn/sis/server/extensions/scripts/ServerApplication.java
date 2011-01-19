package org.iucn.sis.server.extensions.scripts;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

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
		addServiceToRouter(new BaseServiceRestlet(app.getContext()) {
			public Representation handleGet(Request request, Response response) throws ResourceException {
				// TODO Call your script here!
				
				return super.handleGet(request, response);
			}
			@Override
			public void definePaths() {
				paths.add("/start");
			}
		});
		
	}

}
