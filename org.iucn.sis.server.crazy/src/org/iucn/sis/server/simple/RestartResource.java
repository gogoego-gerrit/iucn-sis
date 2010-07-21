package org.iucn.sis.server.simple;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

public class RestartResource extends Resource {

	public RestartResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public Representation represent(final Variant variant) {
		try {
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException unlikely) {
					}
					// crash out of SIS and let wrapper restart it
					System.exit(42);
				}
			}.start();
			return new StringRepresentation("<html><head><body>Restarting SIS Toolkit.</body></html>",
					MediaType.TEXT_HTML);
		} catch (RuntimeException re) {
			re.printStackTrace();
			getResponse().setStatus(Status.SUCCESS_OK);
			return new StringRepresentation("<html><head><body>Could not restart due to error.</body></html>",
					MediaType.TEXT_HTML);
		}
	}
}
