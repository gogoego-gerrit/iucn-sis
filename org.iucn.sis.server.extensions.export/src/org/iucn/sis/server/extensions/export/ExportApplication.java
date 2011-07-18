package org.iucn.sis.server.extensions.export;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.extensions.export.access.exported.GenericExportResource;
import org.restlet.Context;

public class ExportApplication extends SimpleSISApplication {
	
	public static ExportApplication getApplication(Context context) {
		return (ExportApplication) GoGoEgo.get().getApplication(context, "org.iucn.sis.server.extensions.export");
	}
	
	public ExportApplication() {
		super(RunMode.ONLINE);
	}
	
	@Override
	public void init() {
		//addResource(AccessExportResource.class, "/access/{workingsetid}", true);
		//addResource(AccessExportResource.class, "/access", true);
		//addResource(AccessDownloadResource.class, "/download/{file}", true);
		//addResource(AccessDownloadZipResource.class, "/download/zip/{file}", true);
		
		addResource(GenericExportResource.class, "/sources/{source}/{working-set}", false);
	}

}
