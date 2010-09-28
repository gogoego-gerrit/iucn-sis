package org.iucn.sis.server.restlets.taxa;

import org.hibernate.HibernateException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.CommonNameDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.SynonymDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.lwxml.shared.NativeDocument;

public class CommonNameRestlet extends ServiceRestlet{

	public CommonNameRestlet(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void definePaths() {
		paths.add("/taxon/{taxon_id}/commonname/{id}");
		paths.add("/taxon/{taxon_id}/commonname");
	}
	
	protected void addOrEditCommonName(Request request, Response response) {
		
		String text = request.getEntityAsText();
		String taxonID = (String) request.getAttributes().get("taxon_id");
		
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.parseInt(taxonID));
		NativeDocument newDoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		newDoc.parse(text);
		CommonName commonName = CommonName.fromXML(newDoc.getDocumentElement());
		commonName.setIso(SIS.get().getIsoLanguageIO().getIsoLanguageByCode(commonName.getIsoCode()));
		if (commonName.getId() == 0) {
			taxon.getCommonNames().add(commonName);
			commonName.setTaxon(taxon);
			taxon.toXML();
			if (SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request))) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(commonName.getId()+"", MediaType.TEXT_PLAIN);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else {
			
			commonName.setTaxon(taxon);
			taxon.toXML();
			try {
				SISPersistentManager.instance().getSession().merge(commonName);
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				return;
			} 
			
		}
		
	}
	
	protected void deleteCommonName(Request request, Response response) {
		Integer taxonID = Integer.parseInt((String)request.getAttributes().get("taxon_id"));
		Integer id = Integer.parseInt((String)request.getAttributes().get("id"));
		
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(taxonID);
		CommonName toDelete = null;
		for (CommonName syn : taxon.getCommonNames()) {
			if (syn.getId() == id) {
				toDelete = syn;
				break;
			}
		}
		if (toDelete == null) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			taxon.getCommonNames().remove(toDelete);
			taxon.toXML();
			if (SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request))) {
				try {
					CommonNameDAO.delete(toDelete);
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
			addOrEditCommonName(request, response);
		} else if (request.getMethod().equals(Method.DELETE)) {
			deleteCommonName(request, response);
		}
		
	}

}
