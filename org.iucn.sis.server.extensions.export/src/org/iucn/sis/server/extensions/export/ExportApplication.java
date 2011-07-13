package org.iucn.sis.server.extensions.export;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.extensions.export.access.exported.GenericExportResource;
import org.restlet.Context;

public class ExportApplication extends SISApplication {
	
	
	public static ExportApplication getApplication(Context context) {
		return (ExportApplication) GoGoEgo.get().getApplication(context, "org.iucn.sis.server.extensions.export");
	}

	/**
	 * This application is online available online
	 */
	public void initOnline() {
		//addResource(AccessExportResource.class, "/access/{workingsetid}", true);
		//addResource(AccessExportResource.class, "/access", true);
		//addResource(AccessDownloadResource.class, "/download/{file}", true);
		//addResource(AccessDownloadZipResource.class, "/download/zip/{file}", true);
		
		addResource(GenericExportResource.class, "/sources/{source}/{working-set}", false);
	}

}
