package org.iucn.sis.server.extensions.comments;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {

	public ServerApplication() {
		super(RunMode.ONLINE);
	}
	
	/**
	 * Comments available online & offline
	 */
	public void init() {
		addServiceToRouter(new CommentRestlet(app.getContext()));
	}

}
