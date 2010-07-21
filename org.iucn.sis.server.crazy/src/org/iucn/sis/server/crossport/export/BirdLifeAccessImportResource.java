package org.iucn.sis.server.crossport.export;

import org.iucn.sis.server.simple.SISContainerApp;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

public class BirdLifeAccessImportResource extends Resource {

	public BirdLifeAccessImportResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (!BirdLifeAccessImport.isRunning()) {
			new Thread(new BirdLifeAccessImport(SISContainerApp.getStaticVFS(), getContext().getClientDispatcher()))
					.start();
		}
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>"
				+ "The BirdLife data is presently being imported.  "
				+ "Please wait. <p/> <a href='birdlifeImport'>Reload</a></body></html>");
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new StringRepresentation(sb, MediaType.TEXT_HTML));
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public Representation represent(final Variant variant) {
		final StringBuilder sb = new StringBuilder();
		if (BirdLifeAccessImport.isRunning()) {
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>"
					+ "The BirdLife data is presently being imported.  "
					+ "Please wait. <p/> <a href='javascript:window.location.reload()'>Reload</a></body></html>");
		} else {
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");

			sb.append("<form method=\"post\">");
			sb.append("Import BirdLife data.");
			sb.append("<p><input value=\"Import\" type=\"submit\" style='font-family:Verdana; font-size:x-small'/>");
			sb.append("</form>");
			sb.append("</body></html>");
		}

		return new StringRepresentation(sb, MediaType.TEXT_HTML);
	}
}
