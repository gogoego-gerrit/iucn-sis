package org.iucn.sis.server.extensions.bookmarks;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {

	public ServerApplication() {
		super();
	}
	
	@Override
	public void init() {
		addServiceToRouter(new BookmarksRestlet(app.getContext()));
	}

}
