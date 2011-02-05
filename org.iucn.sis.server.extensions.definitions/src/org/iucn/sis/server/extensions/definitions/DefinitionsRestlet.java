package org.iucn.sis.server.extensions.definitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Definition;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.BaseDocumentUtils;

public class DefinitionsRestlet extends BaseServiceRestlet {
	
	private Document definitions = null;
	
	public DefinitionsRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/definitions");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		if (definitions == null) {
			final StringBuilder out = new StringBuilder();
			out.append("<definitions>");
			
			List<Definition> list;
			try {
				list = SIS.get().getManager().listObjects(Definition.class, session);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			for (Definition definition : list)
				out.append(definition.toXML());
			
			out.append("</definitions>");
			
			definitions = BaseDocumentUtils.impl.createDocumentFromString(out.toString());
		}
		
		return new DomRepresentation(MediaType.TEXT_XML, definitions);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		definitions = null;
		
		NativeDocument doc = getEntityAsNativeDocument(entity);
		
		Map<String, Definition> map = new HashMap<String, Definition>();
		try {
			List<Definition> list = SIS.get().getManager().listObjects(Definition.class, session);
			for (Definition definition : list)
				map.put(definition.getName().toLowerCase(), definition);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("definition");
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeElement el = nodes.elementAt(i);
			Definition definition = Definition.fromXML(el);
			String name = definition.getName().toLowerCase();
			try {
				if (map.containsKey(name)) {
					Definition saved = map.get(name);
					saved.setValue(definition.getValue());
					
					SIS.get().getManager().updateObject(session, saved);
					
					map.remove(name);
				}
				else {
					definition.setId(0);
					SIS.get().getManager().saveObject(session, definition);
				}
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		
		for (Definition definition : map.values()) {
			try {
				SIS.get().getManager().deleteObject(session, definition);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}

}
