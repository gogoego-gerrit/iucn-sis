package org.iucn.sis.server.restlets.assessments;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.EditIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Edit;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class AssessmentChangesRestlet extends BaseServiceRestlet {
	
	public AssessmentChangesRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/changes/assessments/{id}");
		paths.add("/changes/assessments/{id}/{mode}/{identifier}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		AssessmentIO io = new AssessmentIO(session);
		
		Assessment assessment = io.getAssessment(getAssessmentID(request));
		if (request.getAttributes().get("mode") == null)
			return showEdits(assessment);
		
		String mode = (String)request.getAttributes().get("mode");
		String identifier = (String)request.getAttributes().get("identifier");
		
		if ("edit".equals(mode)) {
			final EditIO editIO = new EditIO(session);
			final Edit edit;
			try {
				edit = editIO.get(getEditID(request));
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			if (edit == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "The specified edit was not found.");
			
			return findChangesByEdit(assessment, edit, session);
		}
		else if ("field".equals(mode)) {
			return findChangesByField(assessment, identifier, session);
		}
		else
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported mode " + mode + " specified.");
	}
	
	private Representation showEdits(Assessment assessment) {
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		for (Edit edit : assessment.getEdit())
			out.append(edit.toXML());
		out.append("</root>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}
	
	@SuppressWarnings("unchecked")
	private Representation findChangesByEdit(Assessment assessment, Edit edit, Session session) throws ResourceException {
		final List<AssessmentChange> list;
		try {
			//TODO: move to an IO class...
			list = session.createCriteria(AssessmentChange.class)
				.add(Restrictions.eq("assessment", assessment))
				.add(Restrictions.eq("edit", edit))
				.list();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return serialize(list);
	}
	
	@SuppressWarnings("unchecked")
	private Representation findChangesByField(Assessment assessment, String fieldName, Session session) throws ResourceException {
		final List<AssessmentChange> list;
		try {
			//TODO: move to an IO class...
			list = session.createCriteria(AssessmentChange.class)
				.add(Restrictions.eq("assessment", assessment))
				.add(Restrictions.eq("fieldName", fieldName))
				.list();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return serialize(list);
	}
	
	private Representation serialize(List<AssessmentChange> changes) {
		Collections.sort(changes, new AssessmentChangeComparator());
		
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		for (AssessmentChange change : changes)
			out.append(change.toXML());
		out.append("</root>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);	
	}
	
	private Integer getAssessmentID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String)request.getAttributes().get("id"));
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide a valid assessment id.");
		}
	}
	
	private Integer getEditID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String)request.getAttributes().get("identifier"));
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide a valid assessment id.");
		}
	}
	
	private static class AssessmentChangeComparator implements Comparator<AssessmentChange> {
		
		@Override
		public int compare(AssessmentChange o1, AssessmentChange o2) {
			return o2.getEdit().getCreatedDate().compareTo(o1.getEdit().getCreatedDate());
		}
		
	}

}
