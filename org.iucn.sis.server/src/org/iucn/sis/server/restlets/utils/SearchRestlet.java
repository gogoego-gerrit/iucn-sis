package org.iucn.sis.server.restlets.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.CommonNameCriteria;
import org.iucn.sis.server.api.persistance.SynonymCriteria;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SearchRestlet extends ServiceRestlet {

	

	public SearchRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/search");
	}

	
	/**
	 * Currently only a lowest level taxon search, can be extended in the future to search taxon, assessments, and working sets.
	 * 
	 */
	@Override
	public void performService(Request request, Response response) {
		try {
			
			Document terms = new DomRepresentation(request.getEntity()).getDocument();
			Element docElement = terms.getDocumentElement();
			
			NodeList commonName = docElement.getElementsByTagName("commonName");
			NodeList sciName = docElement.getElementsByTagName("sciName");
			NodeList synonym = docElement.getElementsByTagName("synonym");
			String commonNameString;
			String sciNameString;
			String synonymString;
			
			HashSet<Taxon> taxa = new HashSet<Taxon>();
						
			if (commonName.getLength() > 0) {
				TaxonCriteria criteria = getTaxonCriteria();
				commonNameString = "%" + commonName.item(0).getTextContent() + "%" ;
				CommonNameCriteria commonNameCriteria = criteria.createCommonNamesCriteria();
				commonNameCriteria.name.ilike(commonNameString);
				taxa.addAll(search(criteria));
			}
			
			if (synonym.getLength() > 0) {
				TaxonCriteria criteria = getTaxonCriteria();
				synonymString = "%" + synonym.item(0).getTextContent() + "%" ;
				SynonymCriteria synonymCriteria = criteria.createSynonymsCriteria();
				synonymCriteria.friendlyName.ilike(synonymString);
				taxa.addAll(search(criteria));
			}			
			
			if (sciName.getLength() > 0) {
				TaxonCriteria criteria = getTaxonCriteria();
				sciNameString = "%" + sciName.item(0).getTextContent() + "%" ;
				criteria.friendlyName.ilike(sciNameString);
				taxa.addAll(search(criteria));
			}
			
			StringBuilder results = new StringBuilder("<results>\r\n");
			for (Taxon taxon : taxa)
				results.append("<result id=\"" + taxon.getId() + "\"/>");
			results.append("</results>\r\n");
			
			response.setEntity(results.toString(), MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
			
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected Collection<Taxon> search(TaxonCriteria criteria) {
		return Arrays.asList(SIS.get().getTaxonIO().search(criteria));
	}
	
	protected TaxonCriteria getTaxonCriteria() {
		TaxonCriteria criteria = SIS.get().getTaxonIO().getCriteria();
		criteria.createTaxonLevelCriteria().level.ge(TaxonLevel.SPECIES);
		return criteria;
	}
	

}
