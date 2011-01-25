package org.iucn.sis.server.extensions.tags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Marked;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class MarkedObjectRestlet extends BaseServiceRestlet {
	
	public MarkedObjectRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/mark/{username}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		final StringBuilder xml = new StringBuilder();
		xml.append("<root>");
		for (Marked marked : getExisting(request))
			xml.append(marked.toXML());
		xml.append("</root>");
		
		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
		final NativeDocument document = getEntityAsNativeDocument(entity);
		
		final Map<Integer, Marked> existing = new HashMap<Integer, Marked>();
		for (Marked marked : getExisting(request))
			existing.put(marked.getId(), marked);
		
		final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("marked");
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeElement node = nodes.elementAt(i);
			Marked marked = Marked.fromXML(node);
			if (marked.getId() > 0) {
				Marked loaded = existing.remove(marked.getId());
				/*
				 * Should be non-null, otherwise a mark with a 
				 * non-zero ID is reporting existence, but not 
				 * found...
				 */
				if (loaded != null) {
					loaded.setMark(marked.getMark());
					loaded.setObjectid(marked.getObjectid());
					loaded.setType(marked.getType());
					
					try {
						SIS.get().getManager().updateObject(loaded);
					} catch (PersistentException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
			}
			else {
				try {
					SIS.get().getManager().saveObject(marked);
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		}
		
		try {
			for (Marked marked : existing.values()) {
				SIS.get().getManager().deleteObject(marked);
			}
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		response.setEntity(handleGet(request, response));
	}
	
	@SuppressWarnings("unchecked")
	private List<Marked> getExisting(Request request) throws ResourceException {
		try {
			return SIS.get().getManager().getSession().createCriteria(Marked.class).
				add(Restrictions.eq("user", getUser(request))).list();
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}		
	}

}
