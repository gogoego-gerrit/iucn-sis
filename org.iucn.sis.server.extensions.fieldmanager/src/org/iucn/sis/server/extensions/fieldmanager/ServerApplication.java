package org.iucn.sis.server.extensions.fieldmanager;

import javax.naming.NamingException;

import org.gogoego.api.classloader.SimpleClasspathResource;
import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.extensions.fieldmanager.restlets.FieldManagerRestlet;
import org.iucn.sis.server.extensions.fieldmanager.restlets.UIRestlet;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class ServerApplication extends SimpleSISApplication {
	
	private FieldSchemaGenerator generator;
	
	public ServerApplication() {
		super(RunMode.ONLINE);
	}

	@Override
	public void init() {
		addResource(GWTClientResource.class, "/application/manager/client", true);
		
		UIRestlet uiRestlet = new UIRestlet(app.getContext(), generator);
		addResource(uiRestlet, uiRestlet.getPaths(), true);
		
		FieldManagerRestlet service = new FieldManagerRestlet(app.getContext(), generator);
		addResource(service, service.getPaths(), true);
	}
	
	@Override
	public boolean isInstalled() {
		try {
			generator = new FieldSchemaGenerator("sis_lookups");
		} catch (NamingException e) {
			return false;
		}
		
		return true;
	}
	
	public static class GWTClientResource extends SimpleClasspathResource {
		
		public GWTClientResource(Context context, Request request, Response response) {
			super(context, request, response);
			addGZIPHeader();
		}
		
		public String getBaseUri() {
			return "org/iucn/sis/client/fieldmanager/compiled/public/org.iucn.sis.FieldManager";
		}
		
		public ClassLoader getClassLoader() {
			return GoGoEgo.get().getClassLoaderPlugin("org.iucn.sis.client.fieldmanager.compiled");
		}
		
	}

}
