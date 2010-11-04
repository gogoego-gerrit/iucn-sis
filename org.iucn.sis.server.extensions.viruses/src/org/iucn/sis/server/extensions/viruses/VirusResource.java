package org.iucn.sis.server.extensions.viruses;

import java.util.Collection;

import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Virus;
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

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;

public class VirusResource extends Resource {
	
	private final String identifier;
	private final SISPersistentManager manager;

	public VirusResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		identifier = (String)request.getAttributes().get("identifier");
		manager = SISPersistentManager.instance();
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		if (identifier == null)
			return listAll();
		else {
			Integer id;
			try {
				id = Integer.valueOf(identifier);
			} catch (NumberFormatException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid ID");
			}
			
			final Virus virus;
			try {
				virus = manager.getObject(Virus.class, id);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			if (virus == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			
			return new StringRepresentation(virus.toString(), variant.getMediaType());
		}
	}
	
	private Representation listAll() throws ResourceException {
		final Collection<Virus> list;
		try {
			list = manager.listObjects(Virus.class);
		} catch (PersistentException e) {
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
	public void acceptRepresentation(Representation entity) throws ResourceException {
		writeVirus(entity);
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
	public void storeRepresentation(Representation entity) throws ResourceException {
		writeVirus(entity);
	}
	
	private void writeVirus(Representation entity) throws ResourceException {
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
			manager.saveObject(virus);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

}
