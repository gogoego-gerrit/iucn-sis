package org.iucn.sis.server.extensions.export;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.extensions.export.access.exported.AccessDownloadResource;
import org.iucn.sis.server.extensions.export.access.exported.GenericExportResource;

public class ExportApplication extends SimpleSISApplication {
	
	public ExportApplication() {
		super(RunMode.ONLINE);
	}
	
	@Override
	public void init() {
		//addResource(AccessExportResource.class, "/access/{workingsetid}", true);
		//addResource(AccessExportResource.class, "/access", true);
		//addResource(AccessDownloadResource.class, "/download/{file}", true);
		//addResource(AccessDownloadZipResource.class, "/download/zip/{file}", true);
		
		addResource(AccessDownloadResource.class, "/downloads/{file}", true);
		addResource(GenericExportResource.class, "/sources/{source}/{working-set}", false);
	}

}
