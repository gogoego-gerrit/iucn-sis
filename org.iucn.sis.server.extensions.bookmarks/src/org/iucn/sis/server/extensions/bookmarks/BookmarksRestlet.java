package org.iucn.sis.server.extensions.bookmarks;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Bookmark;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class BookmarksRestlet extends BaseServiceRestlet {

	public BookmarksRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/bookmarks");
		paths.add("/bookmarks/{id}");
	}
	
	@SuppressWarnings("unchecked")
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		User user = getUser(request, session);
		if (user == null)
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		
		Integer id = null;
		try {
			id = Integer.valueOf((String)request.getAttributes().get("id"));
		} catch (Exception e) {
			id = null;
		}
		
		Criteria criteria = session.createCriteria(Bookmark.class).add(Restrictions.eq("user", user));
		if (id != null)
			criteria = criteria.add(Restrictions.eq("id", id));
		else
			criteria = criteria.addOrder(Order.desc("date"));
		
		List<Bookmark> list = criteria.list();
		
		final StringBuilder out = new StringBuilder();
		out.append("<bookmarks>");
		
		for (Bookmark bookmark : list)
			out.append(bookmark.toXML());
		
		out.append("</bookmarks>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final Integer id = getBookmarkID(request);
		
		final Bookmark source = Bookmark.fromXML(getEntityAsNativeDocument(entity).getDocumentElement());
		
		final Bookmark target;
		try {
			target = SISPersistentManager.instance().getObject(session, Bookmark.class, id);
		} catch (PersistentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		}
		
		//Pretty much only the name is editable at this point...
		target.setName(source.getName());
		
		try {
			SISPersistentManager.instance().saveObject(session, target);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		final Integer id = getBookmarkID(request);
		
		final Bookmark bookmark;
		try {
			bookmark = SISPersistentManager.instance().getObject(session, Bookmark.class, id);
		} catch (PersistentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		}
		
		try {
			SISPersistentManager.instance().deleteObject(session, bookmark);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final Bookmark bookmark = Bookmark.fromXML(getEntityAsNativeDocument(entity).getDocumentElement());
		if (bookmark.getId() > 0)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Can only PUT new bookmarks, use POST to update.");
		
		bookmark.setUser(getUser(request, session));
		
		try {
			SISPersistentManager.instance().saveObject(session, bookmark);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		response.setStatus(Status.SUCCESS_CREATED);
		response.setEntity(bookmark.toXML(), MediaType.TEXT_XML);
	}
	
	private Integer getBookmarkID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String)request.getAttributes().get("id"));
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Valid bookmark ID required.");
		}
	}

}
