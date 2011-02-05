package org.iucn.sis.server.restlets.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.persistance.CommonNameCriteria;
import org.iucn.sis.server.api.persistance.SynonymCriteria;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.assessments.PublishedAssessmentsComparator;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
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
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		Document terms = getEntityAsDocument(entity);
		
		Element docElement = terms.getDocumentElement();
			
		NodeList commonName = docElement.getElementsByTagName("commonName");
		NodeList sciName = docElement.getElementsByTagName("sciName");
		NodeList synonym = docElement.getElementsByTagName("synonym");
		String commonNameString;
		String sciNameString;
		String synonymString;
		
		TaxonIO taxonIO = new TaxonIO(session);
		AssessmentIO assessmentIO = new AssessmentIO(session);
			
		HashSet<Taxon> taxa = new HashSet<Taxon>();
						
		if (commonName.getLength() > 0) {
			TaxonCriteria criteria = getTaxonCriteria(taxonIO);
			commonNameString = "%" + commonName.item(0).getTextContent() + "%" ;
			CommonNameCriteria commonNameCriteria = criteria.createCommonNamesCriteria();
			commonNameCriteria.name.ilike(commonNameString);
			taxa.addAll(search(criteria, taxonIO));
		}
		
		if (synonym.getLength() > 0) {
			TaxonCriteria criteria = getTaxonCriteria(taxonIO);
			synonymString = "%" + synonym.item(0).getTextContent() + "%" ;
			SynonymCriteria synonymCriteria = criteria.createSynonymsCriteria();
			synonymCriteria.friendlyName.ilike(synonymString);
			taxa.addAll(search(criteria, taxonIO));
		}			
			
		if (sciName.getLength() > 0) {
			TaxonCriteria criteria = getTaxonCriteria(taxonIO);
			sciNameString = "%" + sciName.item(0).getTextContent() + "%" ;
			criteria.friendlyName.ilike(sciNameString);
			taxa.addAll(search(criteria, taxonIO));
		}
		
		final CommonNameComparator comparator = new CommonNameComparator();
			
		StringBuilder results = new StringBuilder("<results>\r\n");
		for (Taxon taxon : taxa) {
			List<CommonName> cname = new ArrayList<CommonName>(taxon.getCommonNames());
			Collections.sort(cname, comparator);
			
			results.append("<result id=\"" + taxon.getId() + "\">");
			results.append(XMLWritingUtils.writeCDATATag("name", taxon.getFullName()));
			results.append(XMLWritingUtils.writeCDATATag("commonName", cname.isEmpty() ? "N/A" : cname.get(0).getName()));
			results.append(XMLWritingUtils.writeCDATATag("level", Taxon.getDisplayableLevel(taxon.getLevel())));
			results.append(XMLWritingUtils.writeCDATATag("category", getCategory(taxon, assessmentIO)));
			results.append(XMLWritingUtils.writeCDATATag("family", 
					taxon.getFootprint().length >= 5 ? taxon.getFootprint()[4] : "N/A"));
			results.append(XMLWritingUtils.writeCDATATag("genus", 
					taxon.getFootprint().length >= 6 ? taxon.getFootprint()[5] : "N/A"));
			results.append("</result>");
		}
		results.append("</results>\r\n");
		
		response.setEntity(results.toString(), MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_OK);
	}
	
	private String getCategory(Taxon taxon, AssessmentIO assessmentIO) {
		List<Assessment> list = 
			assessmentIO.readPublishedAssessmentsForTaxon(taxon);
		
		if (list.isEmpty())
			return "N/A";
		else {
			Collections.sort(list, new PublishedAssessmentsComparator());
		
			Assessment assessment = list.get(0);
			
			RedListCriteriaField proxy = new RedListCriteriaField(assessment.getField(CanonicalNames.RedListCriteria));
			
			String category = proxy.isManual() ? proxy.getManualCategory() : proxy.getGeneratedCategory();
			
			return "".equals(category) ? "N/A" : category;
		}
	}
	
	protected Collection<Taxon> search(TaxonCriteria criteria, TaxonIO taxonIO) {
		return Arrays.asList(taxonIO.search(criteria));
	}
	
	protected TaxonCriteria getTaxonCriteria(TaxonIO taxonIO) {
		TaxonCriteria criteria = taxonIO.getCriteria();
		criteria.createTaxonLevelCriteria().level.ge(TaxonLevel.SPECIES);
		return criteria;
	}
	

}
