package org.iucn.sis.server.extensions.notes;

import java.util.Date;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;

public class NotesRestlet extends BaseServiceRestlet {

	public NotesRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/notes/{type}/{id}");
	}
	
	@Override
	public void handleDelete(Request request, Response response) throws ResourceException {
		final String type = getType(request);
		final Integer id = getID(request);
		
		if (type.equalsIgnoreCase("note")) {
			Notes note = SIS.get().getNoteIO().get(id);
			if (note != null) {
				if (note.getField() != null) {
					note.getField().getNotes().remove(note);
					try {
						FieldDAO.save(note.getField());
					} catch (PersistentException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
				} else if (note.getTaxon() != null) {
					note.getTaxon().getNotes().remove(note);
				}
				SIS.get().getNoteIO().delete(note);
				response.setStatus(Status.SUCCESS_OK);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
			
		} else if (type.equalsIgnoreCase("field")) {
			Field field = SIS.get().getFieldIO().get(id);
			if (field != null) {
				for (Notes note : field.getNotes()) {
					if (!SIS.get().getNoteIO().delete(note)){
						response.setStatus(Status.SERVER_ERROR_INTERNAL);
						return;
					}
				}
				response.setStatus(Status.SUCCESS_OK);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
		} else if (type.equalsIgnoreCase("taxon")) {
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
			
			if (taxon != null) {
				for (Notes note : taxon.getNotes()) {
					if (!SIS.get().getNoteIO().delete(note)){
						response.setStatus(Status.SERVER_ERROR_INTERNAL);
						return;
					}
				}
				response.setStatus(Status.SUCCESS_OK);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
			
		} else 
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid type specified: " + type);
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		final String type = getType(request);
		final Integer id = getID(request);
		
		if (type.equalsIgnoreCase("note")) {
			Notes note = SIS.get().getNoteIO().get(id);
			if (note != null) {
				return new StringRepresentation(note.toXML(), MediaType.TEXT_XML);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
		} else if (type.equalsIgnoreCase("field")) {
			Field field = SIS.get().getFieldIO().get(id);
			if (field != null) {
				StringBuilder xml = new StringBuilder("<xml>");
				for (Notes note : field.getNotes())
					xml.append(note.toXML());
				xml.append("</xml>");
				return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
		} else if (type.equalsIgnoreCase("taxon")) {
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
			if (taxon != null) {
				StringBuilder xml = new StringBuilder("<xml>");
				for (Notes note : taxon.getNotes())
					xml.append(note.toXML());
				xml.append("</xml>");
				return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
		} else if (type.equalsIgnoreCase("assessment")) {
			Assessment assessment = SIS.get().getAssessmentIO().getAssessment(id);
			if (assessment != null) {
				StringBuilder xml = new StringBuilder();
				xml.append("<xml>");
				/**
				 * FIXME: a hibernate SQL query that searched 
				 * the notes table would be nice here...
				 */
				if (assessment.getField() != null)
					for (Field field : assessment.getField()) {
						appendNotes(field, xml);
					}
				xml.append("</xml>");
				return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
		} else
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid type specified: " + type);
	}
	
	private void appendNotes(Field field, StringBuilder xml) {
		Field full = SIS.get().getFieldIO().get(field.getId());
		if (full.getNotes() != null && !full.getNotes().isEmpty()) {
			xml.append("<field name=\"" + full.getName() + "\">");
			for (Notes note : full.getNotes()) 
				xml.append(note.toXML());
			xml.append("</field>");
		}
		for (Field subfield : field.getFields())
			appendNotes(subfield, xml);
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
		/**
		 * FIXME: why are there multiple targets to do the same thing?
		 * Shouldn't POST operations throw not found exceptions when 
		 * trying to edit something that doesn't exist?
		 */
		handlePost(entity, request, response);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		final String type = getType(request);
		final Integer id = getID(request);
		
		if (request.getResourceRef().getQueryAsForm().getFirstValue("option") != null
				&& request.getResourceRef().getQueryAsForm().getFirstValue("option").equals("remove"))
			handleDelete(request, response);
		else {
			NativeDocument document = new JavaNativeDocument();
			document.parse(request.getEntityAsText());
			
			Notes note = Notes.fromXML(document.getDocumentElement());
			
			if (note.getValue() == null)
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No note provided.");
			
			Edit edit = new Edit();
			edit.setUser(SIS.get().getUser(request));
			edit.setCreatedDate(new Date());
			edit.getNotes().add(note);
			note.getEdits().add(edit);
			
			if (type.equalsIgnoreCase("field")) {
				Field field = SIS.get().getFieldIO().get(id);
				if (field != null) {
					field.getNotes().add(note);
					note.getFields().add(field);				
					Assessment assessment = SIS.get().getAssessmentIO().getAssessment(field.getAssessment().getId());
					assessment.getField().remove(assessment.getField(field.getName()));
					assessment.getField().add(field);
					field.setAssessment(assessment);
					if (SIS.get().getNoteIO().save(note)) {
						try {
							FieldDAO.save(field);
						} catch (PersistentException e) {
							throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not save field", e); 
						}
						
						response.setStatus(Status.SUCCESS_OK);
						response.setEntity(note.toXML(), MediaType.TEXT_XML);
					} else {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
					}
				} else {
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
				}
				
			} else if (type.equalsIgnoreCase("taxon")) {
				Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
				if (taxon != null) {
					taxon.getEdits().add(edit);
					edit.getTaxon().add(taxon);
					taxon.getNotes().add(note);
					note.getTaxa().add(taxon);
					if (SIS.get().getNoteIO().save(note)) {					
						response.setStatus(Status.SUCCESS_OK);
						response.setEntity(note.toXML(), MediaType.TEXT_XML);
					} else {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
					}
				} else {
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
				}
			} else 
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid type specified: " + type);
		}		
	}
	
	private Integer getID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String) request.getAttributes().get("id"));
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify an ID", e);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a valid numeric ID", e);
		}
	}
	
	private String getType(Request request) throws ResourceException {
		String value = (String)request.getAttributes().get("type");
		if (value == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a type.");
		
		return value;
	}
}
