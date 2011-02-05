package org.iucn.sis.server.extensions.viruses;

import java.util.Collection;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.TransactionResource;
import org.iucn.sis.shared.api.models.Virus;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.BaseDocumentUtils;

public class VirusResource extends TransactionResource {
	
	private final String identifier;
	private final SISPersistentManager manager;

	public VirusResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		identifier = (String)request.getAttributes().get("identifier");
		manager = SISPersistentManager.instance();
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		if (identifier == null)
			return listAll(session);
		else {
			Integer id;
			try {
				id = Integer.valueOf(identifier);
			} catch (NumberFormatException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid ID");
			}
			
			final Virus virus;
			try {
				virus = manager.getObject(session, Virus.class, id);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			if (virus == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			
			return new StringRepresentation(virus.toString(), variant.getMediaType());
		}
	}
	
	private Representation listAll(Session session) throws ResourceException {
		final Collection<Virus> list;
		try {
			list = manager.listObjects(Virus.class, session);
		} catch (PersistentException e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final StringBuilder out = new StringBuilder();
		out.append("<viruses>");
		
		for (Virus virus : list)
			out.append(virus.toXML());
		
		out.append("</viruses>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}
	
	@Override
	public void acceptRepresentation(Representation entity, Session session) throws ResourceException {
		if (identifier == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply an identifier");
		
		writeVirus(entity, session);
		getResponse().setStatus(Status.SUCCESS_OK);
	}
	
	@Override
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}
	
	@Override
	public void storeRepresentation(Representation entity, Session session) throws ResourceException {
		writeVirus(entity, session);
		getResponse().setStatus(Status.SUCCESS_CREATED);
	}
	
	private void writeVirus(Representation entity, Session session) throws ResourceException {
		final String text;
		try {
			text = entity.getText(); 
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final NativeDocument document = new JavaNativeDocument();
		document.parse(text);
		
		Virus virus = Virus.fromXML(document.getDocumentElement());
		
		try {
			manager.saveObject(session, virus);
		} catch (PersistentException e) {
			BaseDocumentUtils.impl.createErrorDocument("Failed to save due to database error.");
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		getResponse().setEntity(virus.toXML(), MediaType.TEXT_XML);
	}
	
	@Override
	public void removeRepresentations(Session session) throws ResourceException {
		if (identifier == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply an identifier");
		
		Integer id;
		try {
			id = Integer.valueOf(identifier);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid ID");
		}
		
		final Virus virus;
		try {
			virus = manager.getObject(session, Virus.class, id);
		} catch (PersistentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		}
		
		if (virus == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		//TODO: Is this virus being used by a threat??  If so, no deleting?
		
		try {
			manager.deleteObject(session, virus);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		getResponse().setStatus(Status.SUCCESS_OK);
	}

}
