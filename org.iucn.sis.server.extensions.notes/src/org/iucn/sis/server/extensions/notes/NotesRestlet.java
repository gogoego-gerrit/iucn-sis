package org.iucn.sis.server.extensions.notes;

import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.w3c.dom.Document;

public class NotesRestlet extends ServiceRestlet {

	public static Document createDomDocument() {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
		}
		return null;
	}

	public NotesRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/notes/{type}/{id}");
		paths.add("/notes/{type}/{id}");

	}

	private void doDelete(Request request, Response response, Integer id, String type) {
		if (type.equalsIgnoreCase("note")) {
			Notes note = SIS.get().getNoteIO().get(id);
			if (note != null) {
				if (note.getField() != null) {
					note.getField().getAssessment();
				} else if (note.getTaxon() != null) {
					note.getTaxon().getNotes().remove(note);
//					Taxon taxon = SIS.get().getTaxonIO().getTaxonFromVFS(note.getTaxon().getId());
//					taxon.getNotes().remove(note);
//					note.setTaxon(taxon);					
				}
				SIS.get().getNoteIO().delete(note);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
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
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			
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
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		

	}

	private void doGet(Response response, Integer id, String type) {
		if (type.equalsIgnoreCase("note")) {
			Notes note = SIS.get().getNoteIO().get(id);
			if (note != null) {
				response.setEntity(note.toXML(), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else if (type.equalsIgnoreCase("field")) {
			Field field = SIS.get().getFieldIO().get(id);
			if (field != null) {
				StringBuilder xml = new StringBuilder("<xml>");
				for (Notes note : field.getNotes())
					xml.append(note.toXML());
				xml.append("</xml>");
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(xml.toString(), MediaType.TEXT_XML);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			
		} else if (type.equalsIgnoreCase("taxon")) {
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
			if (taxon != null) {
				StringBuilder xml = new StringBuilder("<xml>");
				for (Notes note : taxon.getNotes())
					xml.append(note.toXML());
				xml.append("</xml>");
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(xml.toString(), MediaType.TEXT_XML);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private void doPost(Request request, Response response,  Integer id, String type, User user) {
		String text = request.getEntityAsText();
		Notes note = new Notes();
		note.setValue(text);
		Edit edit = new Edit();
		edit.setUser(user);
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
					response.setStatus(Status.SUCCESS_OK);
					response.setEntity(note.toXML(), MediaType.TEXT_XML);
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
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
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		
	}

	@Override
	public void performService(Request request, Response response) {
		String type = (String) request.getAttributes().get("type");
		String id = (String) request.getAttributes().get("id");

		
		if (type == null || id == null)
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else if (request.getMethod().equals(Method.GET))
			doGet(response, Integer.valueOf(id), type);
		else if (request.getMethod().equals(Method.POST) || request.getMethod().equals(Method.PUT)) {
			if (request.getResourceRef().getQueryAsForm().getFirstValue("option") != null
					&& request.getResourceRef().getQueryAsForm().getFirstValue("option").equals("remove")) {
				doDelete(request, response, Integer.valueOf(id), type);
			} else
				doPost(request, response, Integer.valueOf(id), type, SIS.get().getUser(request));
		}

		else
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}
