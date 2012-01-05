package org.iucn.sis.server.extensions.reports;

import java.io.IOException;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.restlet.Context;
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
		Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
			
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		final Assessment assessment = assessmentIO.getAssessment(id);
		
		if (assessment != null) {
			final ReportTemplate template;
			try {
				template = new AssessmentReportTemplate(session, assessment);
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			template.build();
		
			return new StringRepresentation(template.toString(), MediaType.TEXT_HTML);
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find assessment #" + id);
		}
	}
}
