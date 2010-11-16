package org.iucn.sis.server.extensions.references;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.restlet.resource.Resource;

public class ServerApplication extends SimpleSISApplication {
	
	private static final String PREFIX = "/refsvr";
	
	public void init() {
		addResource("/types", TypesResource.class);
		addResource("/type/{type}", TypeResource.class);
		addResource("/reference/{refid}", ReferenceResource.class);
		addResource("/submit", SubmissionResource.class);
		addResource("/search/reference", ReferenceSearchResource.class);
	}
	
	private void addResource(String path, Class<? extends Resource> resource) {
		addResource(resource, PREFIX + path, false);
	}

}
