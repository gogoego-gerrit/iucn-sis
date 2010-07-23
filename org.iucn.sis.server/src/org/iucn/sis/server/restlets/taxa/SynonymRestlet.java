package org.iucn.sis.server.restlets.taxa;

import org.hibernate.HibernateException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.SynonymDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
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
		System.out.println("this is taxonID " + taxonID);
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.parseInt(taxonID));
		NativeDocument newDoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		newDoc.parse(text);
		Synonym synonymn = Synonym.fromXML(newDoc.getDocumentElement(), null);
		if (synonymn.getId() == 0) {
			taxon.getSynonyms().add(synonymn);
			synonymn.setTaxon(taxon);
			try{
			System.out.println("saving the taxon: \n" + taxon.toXML());
			if (SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request))) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(synonymn.getId()+"", MediaType.TEXT_PLAIN);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			Synonym toRemove = null;
			for (Synonym syn : taxon.getSynonyms()) {
				if (syn.getId() == synonymn.getId()) {
					toRemove = syn;
					break;
				}
			}
//			taxon.getSynonyms().remove(toRemove);
//			taxon.getSynonyms().add(synonymn);
			synonymn.setTaxon(taxon);
			try {
//				SIS.get().getManager().getSession().update(synonymn);
//				synonymn = (Synonym) SIS.get().getManager().getSession().merge(synonymn);
//				SISPersistentManager.instance().getSession().merge(taxon);
				SISPersistentManager.instance().getSession().merge(synonymn);
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				return;
			} 
//			catch (PersistentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				response.setStatus(Status.SERVER_ERROR_INTERNAL);
//				return;
//			}
			
//			SIS.get().getTaxonIO().afterSaveTaxon(taxon);
			
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
