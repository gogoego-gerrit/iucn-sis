package org.iucn.sis.server.crossport.export;

import org.iucn.sis.server.simple.SISContainerApp;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

public class NewAccessExport extends Resource {

	public NewAccessExport(final Context context, final Request request, final Response response) {
		super(context, request, response);
		setReadable(true);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
	
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		final StringBuilder sb = new StringBuilder();

		try {
			if (!SISContainerApp.amIOnline()) {
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>"
						+ "Sorry! This feature is only available in the online environment.</body></html>");
				return new StringRepresentation(sb, MediaType.TEXT_HTML);
			}
		} catch (Exception ignored) {
			// Nothing to do here. No JAR was found.
		}

		if (DBMirrorManager.impl.isExporting()) {
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>"
					+ "The database image is presently being generated.  "
					+ "Please wait. <p/> <a href='javascript:window.location.reload()'>Reload</a></body></html>");
		} else {
			DBMirrorManager.impl.runFullExport();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("<p>Starting full database build...</p>");
			sb.append("</body></html>");
		}

		return new StringRepresentation(sb, MediaType.TEXT_HTML);
	}
}
