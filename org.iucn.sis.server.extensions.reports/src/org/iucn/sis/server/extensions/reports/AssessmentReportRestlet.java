package org.iucn.sis.server.extensions.reports;

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
		boolean showEmpties = true; 
		boolean useLimited = false;
		
		Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
		showEmpties = Boolean.valueOf(queryString.getFirstValue("empty"));
		useLimited = Boolean.valueOf(queryString.getFirstValue("limited"));
			
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		Assessment assessment = assessmentIO.getAssessment(id);
		if (assessment != null) {
		
			Taxon taxa = assessment.getTaxon();
			AssessmentReportTemplate template = new AssessmentReportTemplate(showEmpties, useLimited);
			
			StringBuilder body = new StringBuilder();
			body.append("<html>");
			body.append("<head>");
			body.append("<title>Assessment Report - "+taxa.getFullName()+"</title>");
			body.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../css/reportStyles.css\" />");
			body.append("</head>");
			body.append("<body>");
			body.append(template.buildReportHeading());
			body.append(template.buildTaxonomy(taxa,assessment));
			body.append(template.buildAssessmentInfo(assessment));
			body.append(template.buildGeographicRange(assessment));
			body.append(template.buildPopulation(assessment));
			body.append(template.buildHabitatAndEcology(assessment));
			body.append(template.buildThreats(assessment));
			body.append(template.buildConservationActions(assessment));
			body.append(template.buildBibliography(assessment));
			body.append("</body>");
			body.append("</html>");
		
			response.setStatus(Status.SUCCESS_OK);
			return new StringRepresentation(body.toString(), MediaType.TEXT_HTML);
			
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find assessment #" + id);
		}
	}
}
