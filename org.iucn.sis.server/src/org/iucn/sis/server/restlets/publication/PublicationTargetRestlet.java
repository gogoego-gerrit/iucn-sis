package org.iucn.sis.server.restlets.publication;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.PublicationIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.PublicationTarget;
import org.iucn.sis.shared.api.models.Reference;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;

public class PublicationTargetRestlet extends BaseServiceRestlet {
	
	public PublicationTargetRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/publication/targets");
		paths.add("/publication/targets/{id}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		PublicationIO io = new PublicationIO(session);
		
		final StringBuilder out = new StringBuilder();
		out.append("<targets>");
		
		try {
			for (PublicationTarget target : io.listPublicationTargets())
				out.append(target.toXML());
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
			
		out.append("</targets>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final NativeDocument document = getEntityAsNativeDocument(entity);
		
		PublicationTarget target = PublicationTarget.fromXML(document.getDocumentElement());
		if (target.getReference() != null) {
			Reference reference;
			try {
				reference = SISPersistentManager.instance().getObject(session, Reference.class, target.getReference().getId()); 
			} catch (PersistentException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Reference reported available was not found.");
			}
			
			target.setReference(reference);
		}
		
		PublicationIO io = new PublicationIO(session);
		io.createPublicationTarget(target);
		
		response.setStatus(Status.SUCCESS_CREATED);
		response.setEntity(new StringRepresentation(target.toXML(), MediaType.TEXT_XML));
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		NativeDocument document = getEntityAsNativeDocument(entity);
		PublicationTarget target = PublicationTarget.fromXML(document.getDocumentElement());
		PublicationIO io = new PublicationIO(session);
		io.updateTarget(target);
		
		response.setEntity(new StringRepresentation(target.toXML(), MediaType.TEXT_XML));
	}

	@Override
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		PublicationIO io = new PublicationIO(session);
		io.deleteTarget(toInt((String)request.getAttributes().get("id")));
	}
	
	private Integer toInt(String value) throws ResourceException {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
	
}
