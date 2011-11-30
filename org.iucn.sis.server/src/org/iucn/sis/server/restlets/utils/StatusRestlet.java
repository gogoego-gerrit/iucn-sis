package org.iucn.sis.server.restlets.utils;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.locking.LockException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class StatusRestlet extends BaseServiceRestlet {

	public StatusRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/status/assessment/{id}/{lastModDate}");
		paths.add("/status/taxon/{id}/{lastModDate}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		if (!request.getResourceRef().getPath().contains("/status/assessment"))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
		String modDate = (String) request.getAttributes().get("lastModDate");
		User user = getUser(request, session);

		String ret;
		try {
			ret = SIS.get().getLocker().checkAssessmentAvailability(id, modDate, user, session);
		} catch (LockException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		if (ret == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		return new StringRepresentation(ret, MediaType.TEXT_XML);
	}
	
}
