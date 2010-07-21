package org.iucn.sis.server.extensions.attachments;

import org.iucn.sis.server.api.application.SISActivator;
import org.iucn.sis.server.api.application.SISApplication;

public class ServerActivator extends SISActivator{

	@Override
	protected String getAppDescription() {
		return "SIS File Attachment";
	}
	
	@Override
	protected String getAppName() {
		return "SIS File Attachment";
	}
	
	@Override
	protected SISApplication getInstance() {
		return new ServerApplication();
	}
	
}
