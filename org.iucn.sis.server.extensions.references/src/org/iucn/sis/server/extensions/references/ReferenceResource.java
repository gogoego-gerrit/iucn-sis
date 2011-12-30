package org.iucn.sis.server.extensions.references;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.TransactionResource;
import org.iucn.sis.shared.api.models.Reference;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

@SuppressWarnings("deprecation")
public class ReferenceResource extends TransactionResource {

	public ReferenceResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public boolean allowDelete() {
		return true;
	}
	
	@Override
	public void removeRepresentations(Session session) throws ResourceException {
		final Integer id;
		try {
			id = Integer.valueOf((String)getRequest().getAttributes().get("refid"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid ID", e);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "PLease supply an ID", e);
		}
		
		final Reference reference;
		try {
			reference = SISPersistentManager.instance().getObject(session, Reference.class, id);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		if (!reference.getField().isEmpty() || !reference.getSynonym().isEmpty() || 
				!reference.getTaxon().isEmpty() || !reference.getCommon_name().isEmpty()) {
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}
		
		try {
			SISPersistentManager.instance().deleteObject(session, reference);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	@Override
	public Representation represent(final Variant variant, Session session) throws ResourceException {
		final Integer id;
		try {
			id = Integer.valueOf((String)getRequest().getAttributes().get("refid"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid ID", e);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "PLease supply an ID", e);
		}
		
		final Reference reference;
		try {
			reference = SISPersistentManager.instance().getObject(session, Reference.class, id);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return new StringRepresentation(reference.toXML(), variant.getMediaType());
	}
}
