package org.iucn.sis.server.extensions.reports;

import java.io.IOException;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
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
		paths.add("/reports/{type}/{id}");
	}

	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
		String type = (String)request.getAttributes().get("type");
			
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		final Assessment assessment = assessmentIO.getAssessment(id);
		
		if (assessment != null) {
			if ("redlist".equals(type)) {
				final ReportTemplate template;
				try {
					template = new AssessmentReportTemplate(session, assessment);
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				
				template.build();
			
				return new StringRepresentation(template.toString(), MediaType.TEXT_HTML);
			}
			else if ("full".equals(type)) {
				Form form = request.getResourceRef().getQueryAsForm();
				boolean limitedSet = "true".equals(form.getFirstValue("limited", "false"));
				boolean showEmptyFields = "true".equals(form.getFirstValue("empty", "true"));
				
				final AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, showEmptyFields, limitedSet);
				template.parse(assessment);
				
				return new StringRepresentation(template.getHtmlString(), MediaType.TEXT_HTML);
			}
			else if ("available".equals(type)) {
				Form form = request.getResourceRef().getQueryAsForm();
				boolean limitedSet = "true".equals(form.getFirstValue("limited", "false"));
				boolean showEmptyFields = "true".equals(form.getFirstValue("empty", "true"));
				
				final AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, showEmptyFields, limitedSet);
				template.parse(assessment, "_none_");
				
				return new StringRepresentation(template.getHtmlString(), MediaType.TEXT_HTML);
			}
			else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find assessment #" + id);
		}
	}
}
