package org.iucn.sis.server.crossport.export;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.solertium.db.DBException;

public class ExportApplication extends Application {

	public ExportApplication(final Context context) throws DBException {
		super(context);
	}

	@Override
	public Restlet createRoot() {
		final Router root = new Router(getContext());
		root.attach("/birdlifeImport", BirdLifeAccessImportResource.class);
		root.attach("/access/{workingsetid}", AccessExportResource.class);
		root.attach("/access", AccessExportResource.class);

		root.attach("/download/{file}", AccessDownloadResource.class);
		root.attach("/download/zip/{file}", AccessDownloadZipResource.class);

		return root;
	}
}
