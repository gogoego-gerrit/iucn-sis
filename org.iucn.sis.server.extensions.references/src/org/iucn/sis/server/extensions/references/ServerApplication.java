package org.iucn.sis.server.extensions.references;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.restlet.resource.Resource;

@SuppressWarnings("deprecation")
public class ServerApplication extends SimpleSISApplication {
	
	public static final String PREFIX = "/refsvr";
	
	public void init() {
		addResource("/types", TypesResource.class);
		addResource("/type/{type}", TypeResource.class);
		addResource("/reference/{refid}", ReferenceResource.class);
		addResource("/submit", SubmissionResource.class);
		//addResource("/search/reference", ReferenceSearchResource.class);
		
		addServiceToRouter(new ReferenceSearchResource(app.getContext()));
	}	
	
	private void addResource(String path, Class<? extends Resource> resource) {
		addResource(resource, PREFIX + path, false);
	}

}
