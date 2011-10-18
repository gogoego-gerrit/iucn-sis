package org.iucn.sis.server.restlets.taxa;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.ReferenceIO;
import org.iucn.sis.server.api.io.SynonymIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class SynonymRestlet extends BaseServiceRestlet {

	public SynonymRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/taxon/{taxon_id}/synonym/{id}/reference");
		paths.add("/taxon/{taxon_id}/synonym/{id}");
		paths.add("/taxon/{taxon_id}/synonym");
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final Taxon taxon = getTaxon(request, session);
		final TaxonIO io = new TaxonIO(session);
		
		final NativeDocument newDoc = getEntityAsNativeDocument(entity);
		
		Synonym synonym = Synonym.fromXML(newDoc.getDocumentElement(), taxon);
		
		try {
			io.writeTaxon(taxon, getUser(request, session), "Synonym added.");
		} catch (TaxomaticException e) { 
			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
		
		session.flush();
		
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		if (request.getResourceRef().getPath().contains("reference"))
			addOrRemoveReference(entity, request, response, session);
		else
			updateSynonym(entity, request, response, session);
	}
	
	private void addOrRemoveReference(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final SynonymIO io = new SynonymIO(session);
		final ReferenceIO refIO = new ReferenceIO(session);
		
		final Synonym synonym = io.get(getSynonymID(request));
		if (synonym == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No common name exists with that ID");
		
		final NativeDocument newDoc = getEntityAsNativeDocument(entity);
		
		final StringBuilder log = new StringBuilder();
		final NativeNodeList list = newDoc.getDocumentElement().getElementsByTagName("action");
		for (int i = 0; i < list.getLength(); i++) {
			NativeElement element = list.elementAt(i);
			Integer refID = Integer.valueOf(element.getAttribute("id"));
			String action = element.getTextContent();
			if ("add".equals(action)) {
				refIO.addReference(synonym, refID);
				log.append("<li>Added reference " + refID + "</li>");
			} 
			else if ("remove".equals(action)) {
				if (refIO.removeReference(synonym, refID))
					log.append("<li>Removed reference " + refID + "</li>");
				else
					log.append("<li>Reference " + refID + " not attached</li>");
			}
		}
		
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity("<ul>" + log + "</ul>", MediaType.TEXT_XML);
	}
	
	private void updateSynonym(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final Taxon taxon = getTaxon(request, session);
		
		final Synonym synonym = new SynonymIO(session).get(getSynonymID(request)); 
		if (synonym == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Synonym not found.");
		
		NativeDocument newDoc = getEntityAsNativeDocument(entity);
		
		Synonym.fromXML(synonym, newDoc.getDocumentElement(), null);
		synonym.setTaxon(taxon);
		
		try {
			SISPersistentManager.instance().saveObject(session, synonym);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
					
		taxon.toXML();
			
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
	}
	
	@Override
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		try {
			new SynonymIO(session).delete(getSynonymID(request), getUser(request, session));
		} catch (TaxomaticException e) {
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	private Integer getSynonymID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String)request.getAttributes().get("id"));
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
	}
	
	private Taxon getTaxon(Request request, Session session) throws ResourceException {
		final TaxonIO io = new TaxonIO(session);
		final Taxon taxon;
		try {
			taxon = io.getTaxon(Integer.valueOf((String)request.getAttributes().get("taxon_id")));
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Node not found, or could not be loaded.", e);
		}
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Taxon not found");
		return taxon;
	}

}
