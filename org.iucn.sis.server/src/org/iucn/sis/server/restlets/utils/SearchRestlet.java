package org.iucn.sis.server.restlets.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.CommonNameCriteria;
import org.iucn.sis.server.api.persistance.SynonymCriteria;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.utils.CommonNameComparator;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.util.portable.XMLWritingUtils;

public class SearchRestlet extends BaseServiceRestlet {

	public SearchRestlet(Context context) {
		super(context);
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
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		Document terms = getEntityAsDocument(entity);
		
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
		
		final CommonNameComparator comparator = new CommonNameComparator();
			
		StringBuilder results = new StringBuilder("<results>\r\n");
		for (Taxon taxon : taxa) {
			List<CommonName> cname = new ArrayList<CommonName>(taxon.getCommonNames());
			Collections.sort(cname, comparator);
			
			results.append("<result id=\"" + taxon.getId() + "\">");
			results.append("<taxon>");
			results.append(XMLWritingUtils.writeCDATATag("name", taxon.getFullName()));
			results.append(XMLWritingUtils.writeCDATATag("commonName", cname.isEmpty() ? "N/A" : cname.get(0).getName()));
			results.append(XMLWritingUtils.writeCDATATag("level", Taxon.getDisplayableLevel(taxon.getLevel())));
			//TODO: get category
			results.append(XMLWritingUtils.writeCDATATag("category", "N/A"));
			results.append(XMLWritingUtils.writeCDATATag("family", 
					taxon.getFootprint().length >= 5 ? taxon.getFootprint()[4] : "N/A"));
			results.append(XMLWritingUtils.writeCDATATag("genus", 
					taxon.getFootprint().length >= 6 ? taxon.getFootprint()[5] : "N/A"));
			results.append("</taxon>");
			results.append("</result>");
		}
		results.append("</results>\r\n");
		
		response.setEntity(results.toString(), MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_OK);
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
