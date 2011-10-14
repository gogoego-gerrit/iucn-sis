package org.iucn.sis.server.restlets.assessments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.EditIO;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.RegionConflictException;
import org.iucn.sis.server.utils.AssessmentPersistence;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.io.AssessmentChangePacket;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class AssessmentChangesRestlet extends BaseServiceRestlet {
	
	public AssessmentChangesRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/changes/assessments/{id}");
		paths.add("/changes/assessments/{id}/{mode}");
		paths.add("/changes/assessments/{id}/{mode}/{identifier}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		AssessmentIO io = new AssessmentIO(session);
		
		Assessment assessment = io.getAssessment(getAssessmentID(request));
		Hibernate.initialize(assessment.getEdit());
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
	
	private void saveReferences(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final NativeDocument document = getEntityAsNativeDocument(entity);
		
		String fieldID = document.getDocumentElement().getAttribute("field");
		if ("".equals(fieldID))
			fieldID = null;
		
		if (fieldID == null) {
			Assessment assessment;
			try {
				assessment = (Assessment)session.get(Field.class, Integer.valueOf((String)request.getAttributes().get("id")));
			} catch (NumberFormatException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
			} catch (HibernateException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			if (assessment == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Assessment not found.");
			
			final Set<Reference> references = new HashSet<Reference>();
			final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("reference");
			for (int i = 0; i < nodes.getLength(); i++) {
				NativeElement node = nodes.elementAt(i);
				try {
					references.add(SISPersistentManager.instance().getObject(session, Reference.class, Integer.valueOf(node.getAttribute("id"))));
				} catch (Exception e) {
					Debug.println(e);
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
			
			assessment.setReference(references);
			
			session.update(assessment);
		}
		else {
			Field field;
			try {
				field = (Field)session.get(Field.class, Integer.valueOf(fieldID));
			} catch (NumberFormatException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
			} catch (HibernateException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			if (field == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Field " + fieldID + " not found.");
			
			final Set<Reference> references = new HashSet<Reference>();
			final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("reference");
			for (int i = 0; i < nodes.getLength(); i++) {
				NativeElement node = nodes.elementAt(i);
				try {
					references.add(SISPersistentManager.instance().getObject(session, Reference.class, Integer.valueOf(node.getAttribute("id"))));
				} catch (Exception e) {
					Debug.println(e);
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
			
			field.setReference(references);
			
			session.update(field);
		}
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		if ("references".equals(request.getAttributes().get("mode"))) {
			saveReferences(entity, request, response, session);
			return;
		}
		
		final NativeDocument document = getEntityAsNativeDocument(entity);
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		final AssessmentChangePacket packet = AssessmentChangePacket.fromXML(document.getDocumentElement());
		
		final Assessment assessment = assessmentIO.getAssessment(packet.getAssessmentID());
		if (assessment == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No assessment found for " + packet.getAssessmentID());
		
		final AssessmentPersistence saver = new AssessmentPersistence(session, assessment);
		saver.setAllowAdd(false);
		saver.setAllowDelete(false);
		
		final StringBuilder out = new StringBuilder();
		out.append("<ul>");
		
		for (Field field : packet.getAdditions()) {
			field.setAssessment(assessment);
			assessment.getField().add(field);
			
			saver.addChange(saver.createAddChange(field));
			
			out.append(XMLWritingUtils.writeTag("li", "Added new field " + field.getName() + "."));
		}
		
		for (Field field : packet.getDeletions()) {
			final Field toDelete;
			try {
				toDelete = (Field)session.get(Field.class, field.getId());
			} catch (HibernateException e) {
				out.append("Could not find field " + field.getName() + " with id " + field.getId() + " to delete.");
				continue;
			}
			
			saver.addChange(saver.createDeleteChange(toDelete));
			
			try {
				FieldDAO.deleteAndDissociate(toDelete, session);
			} catch (PersistentException e) {
				Debug.println(e);
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			assessment.getField().remove(toDelete);
			
			out.append(XMLWritingUtils.writeTag("li", "Deleted empty field " + field.getName()));
		}
		
		try {
			saver.sink(packet.getEdits());
			
			for (Field field : packet.getEdits())
				out.append(XMLWritingUtils.writeTag("li", "Edited existing field " + field.getName() + "."));
		} catch (PersistentException e) {
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		out.append("</ul>");
		
		Hibernate.initialize(assessment.getEdit());
		
		//This may or may not need to happen for hibernate reasons...
		assessment.toXML();
		
		/*
		 * If this happens, then some field that should not have been 
		 * removed got removed, and I'd rather fail here than continue 
		 * processing; lest we risk losing data, notes, or references.
		 * 
		 * TODO: add this back; removing the constraint for now until 
		 * this is further tested with the client 
		 */
		/*if (source.getField().size() != target.getField().size())
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Server error: fields not persisted correctly.");*/
		
		if (!assessmentIO.allowedToCreateNewAssessment(assessment))
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, new RegionConflictException());
		
		AssessmentIOWriteResult result = 
			assessmentIO.writeAssessment(assessment, getUser(request, session), true);
		
		if (!result.status.isSuccess())
			throw new ResourceException(result.status, "AssessmentIOWrite threw exception when saving.");
		
		session.flush();
		
		response.setStatus(result.status);
		response.setEntity(assessment.toXML(), MediaType.TEXT_XML);
		
		Debug.println("Updates to assessment #{0}\n{1}", assessment.getId(), packet.toHTML());
		
		if (result.edit == null)
			Debug.println("Error: No edit associated with this change. Not backing up changes.");
		else
			saver.saveChanges(assessment, result.edit);
		
	}
	
	private Representation showEdits(Assessment assessment) {
		final List<Edit> edits = new ArrayList<Edit>(assessment.getEdit());
		Collections.sort(edits, new SortEditByDate());
		
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		for (Edit edit : edits)
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
	
	private static class SortEditByDate implements Comparator<Edit> {
		
		@Override
		public int compare(Edit arg0, Edit arg1) {
			int value;
			
			if (arg0 == null || arg0.getCreatedDate() == null) 
				value = 1;
			else if (arg1 == null || arg1.getCreatedDate() == null) 
				value = 0;
			else
				value = arg0.getCreatedDate().compareTo(arg1.getCreatedDate());
			
			//-1 for reverse order
			return value * -1;
		}
		
	}

}
