package org.iucn.sis.server.restlets.utils;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;

public class TaxaTaggingResource extends Resource {

	private final SISPersistentManager manager;
	private final String tag, mode;

	public TaxaTaggingResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		String tag = (String)request.getAttributes().get("tag");
		if (!("feral".equals(tag) || "invasive".equals(tag)))
			tag = null;
		
		this.tag = tag;
		this.mode = "marked".equals(request.getAttributes().get("mode")) ? "marked" : "unmarked";
		
		manager = SISPersistentManager.instance();
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		if (tag == null || mode == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		Criteria criteria = manager.getSession().createCriteria(Taxon.class);
		//Too many taxa to look up untagged, always search for tagged.
		criteria.add(Restrictions.eq(tag, true));
		
		List<Taxon> list;
		try {
			list = criteria.list();
		} catch (HibernateException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		for (Taxon taxon : list)
			out.append(taxon.toXML());
		out.append("</root>");
				
		return new StringRepresentation(out.toString(), variant.getMediaType());
	}
	
	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final ElementCollection nodes = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("taxon")
		);
		for (Element el : nodes) {
			final Integer id;
			try {
				id = Integer.parseInt(el.getAttribute("id"));
			} catch (NumberFormatException e) {
				Debug.println("Failed to tag taxa with invalid id {0}", el.getAttribute("id"));
				continue;
			}
			
			final Taxon taxon;
			try {
				taxon = manager.loadObject(Taxon.class, id);
			} catch (PersistentException e) {
				Debug.println("Failed to load taxa with id {0}", id);
				continue;
			}
			
			boolean isFlagged = "marked".equals(mode);
			if ("feral".equals(tag))
				taxon.setFeral(isFlagged);
			else if ("invasive".equals(tag))
				taxon.setInvasive(isFlagged);
			
			try {
				manager.saveObject(taxon);
				taxon.toXML();
			} catch (PersistentException e) {
				getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, 
					BaseDocumentUtils.impl.createErrorDocument("Failed to update all taxa")	
				));
				
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		
		getResponse().setStatus(Status.SUCCESS_OK);
	}

}
