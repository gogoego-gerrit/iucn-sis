package org.iucn.sis.server.restlets.taxa;

import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.SynonymDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;

public class SynonymRestlet extends BaseServiceRestlet {

	public SynonymRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/taxon/{taxon_id}/synonym/{id}");
		paths.add("/taxon/{taxon_id}/synonym");
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		String taxonID = (String) request.getAttributes().get("taxon_id");
		
		NativeDocument newDoc = new JavaNativeDocument();
		try {
			newDoc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.parseInt(taxonID));
		
		Synonym synonym = Synonym.fromXML(newDoc.getDocumentElement(), null);
		Set<Notes> notes = new HashSet<Notes>();
		for (Notes note : synonym.getNotes()) {
			if (note.getId() != 0)
				notes.add(SIS.get().getNoteIO().get(note.getId()));
		}
		synonym.setNotes(notes);
		if (synonym.getId() == 0) {
			taxon.getSynonyms().add(synonym);
			synonym.setTaxon(taxon);
			
			try {
				SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request));
			} catch (TaxomaticException e) { 
				throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
			}
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
		} else {
			synonym.setTaxon(taxon);
			try {
				SISPersistentManager.instance().mergeObject(synonym);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
				
			//FIXME: shouldn't this added synonym get saved to the database?
			taxon.getSynonyms().add(synonym);
			
			taxon.toXML();
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
		}
	}
	
	@Override
	public void handleDelete(Request request, Response response) throws ResourceException {
		Integer taxonID = Integer.parseInt((String)request.getAttributes().get("taxon_id"));
		Integer id = Integer.parseInt((String)request.getAttributes().get("id"));
		
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(taxonID);
		Synonym toDelete = null;
		for (Synonym syn : taxon.getSynonyms()) {
			if (syn.getId() == id) {
				toDelete = syn;
				break;
			}
		}
		if (toDelete == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		//FIXME: shouldn't this added synonym get removed from the database for this taxon?
		taxon.getSynonyms().remove(toDelete);
		
		
		taxon.toXML();
		
		try {
			SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request));
		} catch (TaxomaticException e) {
			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
		
		try {
			SynonymDAO.delete(toDelete);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
	}

}
