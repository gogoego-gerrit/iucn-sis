package org.iucn.sis.server.restlets.taxa;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.SynonymDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.lwxml.shared.NativeDocument;

public class SynonymRestlet extends ServiceRestlet{

	public SynonymRestlet(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void definePaths() {
		paths.add("/taxon/{taxon_id}/synonym/{id}");
		paths.add("/taxon/{taxon_id}/synonym");
	}
	
	protected void addOrEditSynonymn(Request request, Response response) {
		
		String text = request.getEntityAsText();
		String taxonID = (String) request.getAttributes().get("taxon_id");
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.parseInt(taxonID));
		NativeDocument newDoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		newDoc.parse(text);
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
				if (SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request))) {
					response.setStatus(Status.SUCCESS_OK);
					response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			} catch (Exception e) {
				Debug.println(e);
			}

		} else {
			synonym.setTaxon(taxon);
			try {
				SISPersistentManager.instance().getSession().merge(synonym);
				taxon.getSynonyms().add(synonym);
				taxon.toXML();
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				Debug.println(e);
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				return;
			}

		}
		
	}
	
	protected void deleteSynonymn(Request request, Response response) {
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
		if (toDelete == null) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			taxon.getSynonyms().remove(toDelete);
			taxon.toXML();
			if (SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request))) {
				try {
					SynonymDAO.delete(toDelete);
				} catch (PersistentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
				response.setStatus(Status.SUCCESS_OK);
			}
		}
	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getMethod().equals(Method.POST)) {
			addOrEditSynonymn(request, response);
		} else if (request.getMethod().equals(Method.DELETE)) {
			deleteSynonymn(request, response);
		}
		
	}

}
