package org.iucn.sis.server.extensions.references;

import org.iucn.sis.server.api.application.SISApplication;
import org.restlet.resource.Resource;

public class ServerApplication extends SISApplication {
	
	private static final boolean ALLOW_ONLINE = true;
	private static final boolean ALLOW_OFFLINE = true;
	private static final String PREFIX = "/refsvr";
	
	public void init() {
		addResource("/types", TypesResource.class);
		addResource("/type/{type}", TypeResource.class);
		addResource("/reference/{refid}", ReferenceResource.class);
		addResource("/submit", SubmissionResource.class);
		addResource("/search/reference", ReferenceSearchResource.class);
	}
	
	private void addResource(String path, Class<? extends Resource> resource) {
		addResource(resource, PREFIX + path, ALLOW_ONLINE, ALLOW_OFFLINE, false);
	}

}
