package org.iucn.sis.server.restlets.utils;

import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class LanguageRestlet extends BaseServiceRestlet {
	
	public LanguageRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/languages");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final List<IsoLanguage> list;
		try {
			list = SIS.get().getManager().listObjects(IsoLanguage.class, session);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		for (IsoLanguage language : list)
			out.append(language.toXML());
		out.append("</root>");
		
		//TODO: cache to the VFS
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
		
	}

}
