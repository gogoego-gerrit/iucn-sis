package org.iucn.sis.server.extensions.attachments;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.extensions.attachments.storage.AttachmentStorage;
import org.iucn.sis.server.extensions.attachments.storage.VFSAttachmentStorage;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.vfs.VFSPathToken;

public class FileAttachmentUploadRestlet extends BaseServiceRestlet {
	
	private final AttachmentStorage storage;
	
	public FileAttachmentUploadRestlet(Context context) {
		super(context);
		
		storage = new VFSAttachmentStorage(SIS.get().getVFS());
	}

	@Override
	public void definePaths() {
		paths.add("/assessments/{assessmentID}");
		paths.add("/assessments/{assessmentID}/{attachmentID}");
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final Integer assessmentID = getAssessmentID(request);
		final Integer attachmentID = getAttachmentID(request);
		
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		final AttachmentIO io = new AttachmentIO(session);
		
		final Assessment assessment = assessmentIO.getAssessment(assessmentID);
		if (assessment == null) {
			response.setEntity("Error: Assessment not found.", MediaType.TEXT_PLAIN);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		final RestletFileUpload fileUploaded = new RestletFileUpload(new DiskFileItemFactory());
		
		final List<FileItem> list;
		try {
			list = fileUploaded.parseRequest(request);
		} catch (FileUploadException e) {
			response.setEntity("Error: Failed to read file.", MediaType.TEXT_PLAIN);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		List<String> validFields = new ArrayList<String>();
		for (String fieldName : CanonicalNames.attachable)
			validFields.add(fieldName);
		
		boolean isPublished = false;
		List<String> fields = null;
		String userID = null;
		
		FileItem file = null;

		for (FileItem item : list) {
			if (item.isFormField()) {
				if (item.getFieldName().equalsIgnoreCase("publish"))
					isPublished = !"false".equals(item.getString()) || !"no".equals(item.getString());
				else if (item.getFieldName().equals("user"))
					userID = item.getString();
				else if (validFields.contains(item.getFieldName())) {
					if (fields == null)
						fields = new ArrayList<String>();
					
					fields.add(item.getFieldName());
				}
			} else
				file = item;
		}

		if (attachmentID == null) {
			if (file == null || assessmentID == null || fields == null) {
				response.setEntity("Error: File, publish status, fields, and assessment ID must be sent in order to attach to assessment.", MediaType.TEXT_PLAIN);
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}
		else {
			if (file == null || assessmentID == null) {
				response.setEntity("Error: File and assessment ID must be sent in order to attach to assessment.", MediaType.TEXT_PLAIN);
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}
		
		if (userID == null)
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		
		final User user;
		try {
			user = SISPersistentManager.instance().getObject(session, User.class, Integer.valueOf(userID));
		} catch (PersistentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN); 
		}
		
		if (user == null)
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		final FieldAttachment attachment;
		if (attachmentID == null) {
			attachment = io.createAttachment(cleanFilename(file.getName()), "unsaved", isPublished, user); 
			for (String fieldName : fields) {
				Field field = assessment.getField(fieldName);
				if (field == null) {
					field = new Field();
					field.setName(fieldName);
					field.setAssessment(assessment);
					
					assessment.getField().add(field);
				}
				
				Hibernate.initialize(field.getFieldAttachment());
				
				field.getFieldAttachment().add(attachment);
			
				try {
					SISPersistentManager.instance().saveObject(session, field);
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		}
		else
			attachment = io.getAttachment(attachmentID);
		
		if (attachment == null) {
			response.setEntity("Error: Could not find the specified attachment.", MediaType.TEXT_PLAIN);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "The attacment was not found");
		}
		
		if ("unsaved".equals(attachment.getKey())) {
			try {
				attachment.setKey(storage.save(assessment, attachment, file));
			} catch (ResourceException e) {
				if (e.getStatus().getCode() == Status.CLIENT_ERROR_CONFLICT.getCode())
					response.setEntity("Conflict: File with this name for this assessment already exists.", MediaType.TEXT_PLAIN);
				else
					response.setEntity("Error: Could not persist file due to server error, please try again later.", MediaType.TEXT_PLAIN);
				
				throw e;
			}
		}
		else {
			try {
				storage.update(assessment, attachment, file);
			} catch (ResourceException e) {
				response.setEntity("Error: Failed to update file due to server error.", MediaType.TEXT_PLAIN);
				
				throw e;
			}
			
			Edit edit = new Edit();
			edit.setUser(user);
			edit.getAttachments().add(attachment);
			
			attachment.getEdits().add(edit);
		}
		
		try {
			io.saveMetadata(attachment);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
		
		response.setEntity("Success: Upload successful.", MediaType.TEXT_PLAIN);
		response.setStatus(Status.SUCCESS_OK);
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
	
	/**
	 * Removes characters from a filename that aren't web-friendly.
	 * 
	 * @param name
	 *            the original file name.
	 * @return the cleaned filename
	 */
	private String cleanFilename(String name) {
		String clean = name.replaceAll(" ", "_");
		clean = clean.replaceAll("\\+", "_");
		clean = clean.replaceAll(",", "_");
		clean = clean.replaceAll("&", "_");
		clean = clean.replaceAll("\\?", "_");
		
		return convertWindowsPath(clean);
	}
	
	private String convertWindowsPath(String name) {
		String clean = name;
		VFSPathToken token;
		try {
			token = new VFSPathToken(clean);
		} catch (IllegalArgumentException e) {
			clean = clean.replace('\\', '/');
			if (clean.endsWith("/"))
				clean = clean.substring(0, clean.length() - 1);
			final String[] pathParts = clean.split("/");
			try {
				token = new VFSPathToken(pathParts[pathParts.length - 1]);
			} catch (IllegalArgumentException f) {
				token = new VFSPathToken("new_unresolved_file.tmp");
			}
		}
		return token.toString();
	}

}
