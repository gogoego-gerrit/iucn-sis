package org.iucn.sis.server.restlets.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.TaxonIO;
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

import com.solertium.util.TrivialExceptionHandler;
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
		NodeList level = docElement.getElementsByTagName("level");
		
		int taxonLevel = TaxonLevel.SPECIES;
		
		TaxonIO taxonIO = new TaxonIO(session);
		AssessmentIO assessmentIO = new AssessmentIO(session);
		
		Disjunction disjunction = Restrictions.disjunction(); 
		boolean hasQuery = false;
								
		if (commonName.getLength() > 0) {
			hasQuery = true;
			disjunction.add(Restrictions.ilike("CommonNames.name", clean(commonName.item(0).getTextContent()), MatchMode.ANYWHERE));
		}
		
		if (synonym.getLength() > 0) {
			hasQuery = true;
			disjunction.add(Restrictions.ilike("Synonyms.friendlyName", clean(synonym.item(0).getTextContent()), MatchMode.ANYWHERE));
		}			
			
		if (sciName.getLength() > 0) {
			hasQuery = true;
			disjunction.add(Restrictions.ilike("friendlyName", clean(sciName.item(0).getTextContent()), MatchMode.ANYWHERE));
		}
		
		if (level.getLength() > 0) {
			try {
				taxonLevel = Integer.valueOf(level.item(0).getTextContent());
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		
		final Collection<Taxon> taxa;
		if (hasQuery) {
			TaxonCriteria search = new TaxonCriteria(session.createCriteria(Taxon.class)
				.createAlias("CommonNames", "CommonNames", Criteria.LEFT_JOIN)
				.createAlias("Synonyms", "Synonyms", Criteria.LEFT_JOIN)
				.add(disjunction)
				.addOrder(Order.asc("friendlyName"))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
			search.createTaxonLevelCriteria().level.ge(taxonLevel);
			
			taxa = search(search, taxonIO);
		}
		else
			taxa = new ArrayList<Taxon>();
		
		final CommonNameComparator comparator = new CommonNameComparator();
			
		StringBuilder results = new StringBuilder("<results>\r\n");
		for (Taxon taxon : taxa) {
			List<CommonName> cname = new ArrayList<CommonName>(taxon.getCommonNames());
			Collections.sort(cname, comparator);
			
			results.append("<result id=\"" + taxon.getId() + "\">");
			results.append(XMLWritingUtils.writeCDATATag("name", taxon.getFullName()));
			results.append(XMLWritingUtils.writeCDATATag("status", taxon.getTaxonStatus().getCode()));
			results.append(XMLWritingUtils.writeCDATATag("commonName", cname.isEmpty() ? "N/A" : cname.get(0).getName()));
			results.append(XMLWritingUtils.writeCDATATag("level", Taxon.getDisplayableLevel(taxon.getLevel())));
			//results.append(XMLWritingUtils.writeCDATATag("category", getCategory(taxon, assessmentIO)));
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
	
	private String clean(String value) {
		return SIS.get().getQueries().cleanSearchTerm(value);
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
}
