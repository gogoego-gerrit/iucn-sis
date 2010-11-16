package org.iucn.sis.server.extensions.attachments;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;

public class ServerApplication extends SimpleSISApplication {

	public ServerApplication() {
		super(RunMode.ONLINE);
	}

	/**
	 * Attachments are available online only.
	 */
	public void init() {
		addServiceToRouter(new FileAttachmentRestlet(SIS.get().getVfsroot(), app.getContext()));
	}

}
