package org.iucn.sis.server.extensions.export;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.extensions.export.access.exported.AccessDownloadResource;
import org.iucn.sis.server.extensions.export.access.exported.AccessDownloadZipResource;
import org.iucn.sis.server.extensions.export.access.exported.AccessExportResource;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class ExportApplication extends SISApplication{
	
	
	
	@Override
	public Restlet getPublicRouter() {
		final Router root = new Router(app.getContext());
		root.attach("/access/{workingsetid}", AccessExportResource.class);
		root.attach("/access", AccessExportResource.class);
		root.attach("/download/{file}", AccessDownloadResource.class);
		root.attach("/download/zip/{file}", AccessDownloadZipResource.class);
		return root;
	}
	
	
	public static ExportApplication getApplication(Context context) {
		return (ExportApplication) GoGoEgo.get().getApplication(context, "org.iucn.sis.server.extensions.export");
	}
	

	@Override
	public void init() {
		addResource(AccessExportResource.class, "/access/{workingsetid}", true, false, true);
		addResource(AccessExportResource.class, "/access", true, false, true);
		addResource(AccessDownloadResource.class, "/download/{file}", true, false, true);
		addResource(AccessDownloadZipResource.class, "/download/zip/{file}", true, false, true);
	}

}
