package org.iucn.sis.server.extensions.reports;

import java.io.IOException;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

/**
 * 
 * @author rasanka.jayawardana
 * 
 */
public class AssessmentReportRestlet extends BaseServiceRestlet {

	public AssessmentReportRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {		
		paths.add("/reports/redlist/{id}");
	}

	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		
		Form queryString = request.getResourceRef().getQueryAsForm();
		boolean showSpecial = false; 
		
		Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
		showSpecial = Boolean.valueOf(queryString.getFirstValue("special"));
			
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		Assessment assessment = assessmentIO.getAssessment(id);
		if (assessment != null) {
			
			Taxon taxa = assessment.getTaxon();
			AssessmentReportTemplate template = new AssessmentReportTemplate(showSpecial);
			StringBuilder body = new StringBuilder();
			try{
				template.readHTMLTemplate();
			}catch(IOException e){
				e.printStackTrace();
			}				
			
			template.buildTaxonomy(taxa,assessment);
			template.buildAssessmentInfo(assessment);
			template.buildGeographicRange(assessment);
			template.buildPopulation(assessment);
			template.buildHabitatAndEcology(assessment);
			template.buildThreats(assessment);
			template.buildConservationActions(assessment);
			template.buildBibliography(assessment);
			template.buildCitation(assessment);
			body.append(template.getHTMLString());
		
			response.setStatus(Status.SUCCESS_OK);
			return new StringRepresentation(body.toString(), MediaType.TEXT_HTML);
			
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find assessment #" + id);
		}
	}
}
