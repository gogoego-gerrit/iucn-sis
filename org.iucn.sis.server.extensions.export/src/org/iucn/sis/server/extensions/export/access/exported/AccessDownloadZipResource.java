package org.iucn.sis.server.extensions.export.access.exported;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

public class AccessDownloadZipResource extends Resource {

	public AccessDownloadZipResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.APPLICATION_ZIP));
	}

	@Override
	public Representation represent(final Variant variant) {
		String file = (String) getRequest().getAttributes().get("file");
		String working;

		if (file == null)
			working = AccessExport.getWorking();
		else
			working = "/usr/data/" + file;

		try {
			return new InputRepresentation(new FileInputStream(new File(working)), MediaType.APPLICATION_ZIP);
		} catch (final FileNotFoundException fnf) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("No latest Access database image found to download", MediaType.TEXT_PLAIN);
		}
	}

}
