package org.iucn.sis.server.extensions.attachments;

import java.util.HashSet;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.extensions.attachments.storage.AttachmentStorage;
import org.iucn.sis.server.extensions.attachments.storage.VFSAttachmentStorage;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;

public class FileAttachmentRestlet extends BaseServiceRestlet {

	private final AttachmentStorage storage;

	public FileAttachmentRestlet(Context context) {
		super(context);
		
		storage = new VFSAttachmentStorage(SIS.get().getVFS());
	}
	
	public void definePaths() {
		//Handle GET, PUT, DELETE
		paths.add("/browse/assessments/{assessmentID}");
		paths.add("/browse/assessments/{assessmentID}/{attachmentID}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final Integer assessmentID = getAssessmentID(request);
		final Integer attachmentID = getAttachmentID(request);
		
		final AttachmentIO io = new AttachmentIO(session);
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		
		final Assessment assessment = assessmentIO.getAssessment(assessmentID);
		if (assessment == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Assessment not found.");
		
		/*
		 * TODO: write a better query at the AssessmentIO level to 
		 * avoid these duplicates from coming across...
		 */
		final HashSet<Integer> seen = new HashSet<Integer>();
		
		if (attachmentID == null) {
			final StringBuilder out = new StringBuilder();
			out.append("<attachments>");
			
			for (FieldAttachment attachment : io.getAttachments(assessment))
				if (seen.add(attachment.getId()))
					out.append(attachment.toXML());
			
			out.append("</attachments>");
			
			return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
		}
		else {
			FieldAttachment attachment = io.getAttachment(attachmentID);
			if (attachment == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Attachment not found.");
			
			InputRepresentation representation = 
				new InputRepresentation(storage.get(attachment.getKey()));
			if ("true".equals(request.getResourceRef().getQueryAsForm().getFirstValue("download"))) {
				representation.setDownloadable(true);
				representation.setDownloadName(attachment.getName());
			}
			
			return representation;
		}
	}
	
	@Override
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		final Integer attachmentID = getAttachmentID(request);
		
		if (attachmentID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply an attachment ID.");
		
		final AttachmentIO io = new AttachmentIO(session);
		
		final FieldAttachment attachment = io.getAttachment(attachmentID);
		if (attachment == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Attachment not found, could not delete.");
		
		try {
			io.delete(attachment);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		/*
		 * Force a flush to commit the changes.  If something fails, 
		 * the on-disk file will not be deleted.
		 */
		session.flush();
		
		/*
		 * Now safe to attempt to remove on-disk file.  Also, I am 
		 * catching exceptions here as I don't care if it does not 
		 * actually delete.
		 */
		try {
			storage.delete(attachment.getKey());
		} catch (ResourceException e) {
			Debug.println("The attachment {0} with key {1} failed to delete from the storage server.", attachment.getName(), attachment.getKey());
		}

		response.setStatus(Status.SUCCESS_OK);
		response.setEntity("Attachment deleted", MediaType.TEXT_PLAIN);
	}
	

	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final Integer assessmentID = getAssessmentID(request);
		final Integer attachmentID = getAttachmentID(request);
		
		final AttachmentIO io = new AttachmentIO(session);
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		
		final Assessment assessment = assessmentIO.getAssessment(assessmentID);
		if (assessment == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Assessment not found.");
		
		NativeDocument document = new JavaNativeDocument();
		try {
			document.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		FieldAttachment source = FieldAttachment.fromXML(document.getDocumentElement(), new FieldAttachment.FieldFinder() {
			public Field get(String id, String name) {
				return assessment.getField(name);
			}
		});
		
		if (!attachmentID.equals(source.getId()))
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Entity ID does not match request ID.");
		
		FieldAttachment target = io.getAttachment(source.getId());
		if (target == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Attachment not found, could not delete.");
		
		target.setPublish(source.getPublish());
		target.setName(source.getName());
		target.setFields(source.getFields()); //FieldFinder ensures this is correct.
		
		try {
			io.saveMetadata(target);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	protected Integer getAssessmentID(Request request) throws ResourceException {
		String id = (String) request.getAttributes().get("assessmentID");
		if (id == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify an assessment.");
		
		try {
			return Integer.valueOf(id);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a valid assessment ID.");
		}
	}
	
	protected Integer getAttachmentID(Request request) throws ResourceException {
		String value = (String) request.getAttributes().get("attachmentID");
		if (value == null)
			return null; //Ok to be missing.
		
		try {
			return Integer.valueOf(value);
		} catch (Exception e) { //Not OK to be wrong.
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a valid attachment ID.");
		}
	}	

}
