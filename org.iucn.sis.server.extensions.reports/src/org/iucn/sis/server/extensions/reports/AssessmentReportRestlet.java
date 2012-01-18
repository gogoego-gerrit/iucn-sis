package org.iucn.sis.server.extensions.reports;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.WorkingSet;
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
	
	private final Map<String, String> knownLogos;

	public AssessmentReportRestlet(Context context) {
		super(context);
		
		knownLogos = new ConcurrentHashMap<String, String>();
		knownLogos.put("org.iucn.sis.server.schemas.redlist", "redListLogo.jpg");
		knownLogos.put("org.iucn.sis.server.schemas.birdlife", "birdlifelogo.gif");
	}

	@Override
	public void definePaths() {		
		paths.add("/reports/{type}/{id}");
	}

	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final String type = (String)request.getAttributes().get("type");
		final Form form = request.getResourceRef().getQueryAsForm();
		
		if ("redlist".equals(type)) {
			final ReportTemplate template;
			try {
				template = new AssessmentReportTemplate(session, getAssessment(request, session));
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			template.build();
		
			return new StringRepresentation(template.toString(), MediaType.TEXT_HTML);
		}
		else if ("full".equals(type)) {
			boolean limitedSet = "true".equals(form.getFirstValue("limited", "false"));
			boolean showEmptyFields = "true".equals(form.getFirstValue("empty", "true"));
			
			Assessment assessment = getAssessment(request, session);
			String logo = knownLogos.get(assessment.getSchema(SIS.get().getDefaultSchema()));
			
			final AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, assessment, showEmptyFields, limitedSet);
			if (logo != null)
				template.setLogo(logo);
			template.parse();
			
			Representation representation;
			if ("word".equals(form.getFirstValue("version"))) {
				representation = new StringRepresentation(template.getHtmlString(), MediaType.APPLICATION_WORD);
				representation.setDownloadable(true);
				representation.setDownloadName("report.doc");
			}
			else
				representation = new StringRepresentation(template.getHtmlString(), MediaType.TEXT_HTML);
			
			return representation;
		}
		else if ("available".equals(type)) {
			boolean limitedSet = "true".equals(form.getFirstValue("limited", "false"));
			boolean showEmptyFields = "true".equals(form.getFirstValue("empty", "true"));
			
			Assessment assessment = getAssessment(request, session);
			String logo = knownLogos.get(assessment.getSchema(SIS.get().getDefaultSchema()));
			
			final AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, assessment, showEmptyFields, limitedSet);
			if (logo != null)
				template.setLogo(logo);
			template.parseAvailable();
			
			return new StringRepresentation(template.getHtmlString(), MediaType.TEXT_HTML);
		}
		else
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		String type = (String)request.getAttributes().get("type");
		
		if ("workingset".equals(type)) {
			Form form = request.getResourceRef().getQueryAsForm();
			boolean limitedSet = "true".equals(form.getFirstValue("limited", "false"));
			boolean showEmptyFields = "true".equals(form.getFirstValue("empty", "true"));
			boolean single = "true".equals(form.getFirstValue("single", "true"));
			String logo = knownLogos.get(SIS.get().getDefaultSchema());
			
			final AggregateReporter reporter = new AggregateReporter(session, getWorkingSet(request, session), getUser(request, session));
			if (logo != null)
				reporter.setLogo(logo);
			
			String file = reporter.generate(showEmptyFields, limitedSet, single, getEntityAsNativeDocument(entity));
			
			response.setEntity(file, MediaType.TEXT_PLAIN);
		}
		else
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
	}
	
	private WorkingSet getWorkingSet(Request request, Session session) throws ResourceException {
		final Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
		
		final WorkingSetIO io = new WorkingSetIO(session);
		final WorkingSet ws = io.readWorkingSet(id);
		if (ws == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find working set #" + id);
		
		return ws;
	}
	
	private Assessment getAssessment(Request request, Session session) throws ResourceException {
		final Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
		
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		final Assessment assessment = assessmentIO.getAssessment(id);
		if (assessment == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find assessment #" + id);
		
		return assessment;
	}
}
