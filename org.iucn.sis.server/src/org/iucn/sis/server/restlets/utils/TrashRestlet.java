package org.iucn.sis.server.restlets.utils;

import java.util.Calendar;
import java.util.Date;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.FormattedDate;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.io.AssessmentIOMessage;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TrashRestlet extends BaseServiceRestlet {

	public TrashRestlet(Context context) {
		super(context);
	}

	public void definePaths() {
		paths.add("/trash/{action}");
		paths.add("/trash/{action}/{option1}");
	}

	private void handleDelete(Representation entity, Request request, Response response, Session session) throws ResourceException, PersistentException {
		Document doc = getEntityAsDocument(entity);
		
		Element element = (Element) doc.getDocumentElement().getElementsByTagName("data").item(0);
		String id = element.getAttribute("id");
		String type = element.getAttribute("type");
		
		AssessmentIO assessmentIO = new AssessmentIO(session);
		TaxonIO taxonIO = new TaxonIO(session);
			
		boolean success = false;
		String message = null;
		if (type.equalsIgnoreCase("taxon")) {
			success = taxonIO.permanentlyDeleteTaxon(Integer.parseInt(id));
			message = "Unable to delete taxon " + id;
		} else if (type.equalsIgnoreCase("assessment")) {
			success = assessmentIO.permenantlyDeleteAssessment(Integer.valueOf(id), getUser(request, session));
			message = "Unable to delete assessment " + id;
		} else {
			message = "Invalid type -- should be assessment or taxon.";
		}
		if (success) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setEntity(message, MediaType.TEXT_PLAIN);
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		response.setEntity(message, MediaType.TEXT_PLAIN);
	}

	private void handleDeleteAll(Request request, Response response, Session session) {
		AssessmentIO assessmentIO = new AssessmentIO(session);
		TaxonIO taxonIO = new TaxonIO(session);
		
		boolean success = assessmentIO.permenantlyDeleteAllTrashedAssessments() && taxonIO.permenantlyDeleteAllTrashedTaxa();
		if (success) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity("All trashed items have been deleted.", MediaType.TEXT_PLAIN);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity("Items could not be deleted.", MediaType.TEXT_PLAIN);
		}
	}
	
	/**
	 * Gets all assessments and taxa that have been deleted
	 * 
	 * @param request
	 * @param response
	 * @param ec
	 */
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		Date defaultDate = Calendar.getInstance().getTime();
		StringBuilder xml = new StringBuilder("<trash>");
		
		AssessmentIO assessmentIO = new AssessmentIO(session);
		TaxonIO taxonIO = new TaxonIO(session);
		
		Assessment[] assessments;
		try {
			assessments = assessmentIO.getTrashedAssessments();
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
		
		for (Assessment assessment : assessments) {
			xml.append("<data id=\"" + assessment.getId() + "\" ");
			xml.append("type=\"assessment\" ");
			xml.append("status=\"" + assessment.getAssessmentType().getDisplayName() + "\" ");
			if (!assessment.getEdit().isEmpty()) {
				Edit last = assessment.getLastEdit();
				xml.append("user=\"" + last.getUser().getUsername() + "\" ");
				xml.append("date=\"" + FormattedDate.impl.getDate(last.getCreatedDate())
						+ "\" ");
			}
			else {
				xml.append("user=\"Unknown\" ");
				xml.append("date=\"" + FormattedDate.impl.getDate(defaultDate) + "\" ");
			}
			xml.append("node=\"" + assessment.getSpeciesName() + "\" ");
			xml.append("display=\"\" ");
			xml.append("/>");
		}

		Taxon[] taxa;
		try {
			taxa = taxonIO.getTrashedTaxa();
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
		
		for (Taxon taxon : taxa) {
			xml.append("<data id=\"" + taxon.getId() + "\" ");
			xml.append("type=\"taxon\" ");
			xml.append("status=\"\" ");
			if (!taxon.getEdits().isEmpty()) {
				Edit last = taxon.getLastEdit();
				xml.append("user=\"" + last.getUser().getUsername() + "\" ");
				xml.append("date=\"" + FormattedDate.impl.getDate(last.getCreatedDate()) + "\" ");
			}
			else {
				xml.append("user=\"Unknown\" ");
				xml.append("date=\"" + FormattedDate.impl.getDate(defaultDate) + "\" ");	
			}
			xml.append("node=\"" + taxon.getFullName() + "\" ");
			xml.append("display=\"\" ");
			xml.append("/>");
		}
		xml.append("</trash>");
		
		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
	}

	private void handleRestore(Representation entity, Request request, Response response, Session session) throws ResourceException, PersistentException {
		String restoreRelatedAssessments = (String) request.getAttributes().get("option1");
		
		Document doc = getEntityAsDocument(entity);
		Element element = (Element) doc.getDocumentElement().getElementsByTagName("data").item(0);
		String id = element.getAttribute("id");
		String type = element.getAttribute("type");
		
		User user = getUser(request, session);
		AssessmentIO assessmentIO = new AssessmentIO(session);
		TaxonIO taxonIO = new TaxonIO(session);

		boolean success = false;
		String message = null;
		if (type.equalsIgnoreCase("taxon")) {
			try {
				taxonIO.restoreTrashedTaxon(Integer.valueOf(id), user);
			} catch (TaxomaticException e) {
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, e.getErrorAsDocument()));
				response.setStatus(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL);
				return;
			}
				
			if (restoreRelatedAssessments != null && restoreRelatedAssessments.equalsIgnoreCase("true")) {
				AssessmentIOMessage m = assessmentIO.restoreDeletedAssessmentsAssociatedWithTaxon(Integer
						.valueOf(id), getUser(request, session));
				message = m.getMessage();
				if (m.getFailed() == null || m.getFailed().isEmpty()) {
					success = true;
				}
			} else {
				success = true;
			}
		} else if (type.equalsIgnoreCase("assessment")) {
			success = assessmentIO.restoreTrashedAssessments(Integer.valueOf(id), user).status.isSuccess();
			if (success) {
				Taxon taxon = assessmentIO.getNonCachedAssessment(Integer.valueOf(id)).getTaxon();
				if (taxon.getState() == Taxon.DELETED) {
					try {
						taxonIO.restoreTrashedTaxon(taxon.getId(), user);
					} catch (TaxomaticException e) {
						Debug.println(e);
						success = false;
					}
				}
			}
		} else {
			message = "Invalid type -- should be assessment or taxon.";
		}
		if (success && message == null)
			message = "Successfully restored " + type;
		else if (!success && message == null)
			message = "Unable to find " + type;

		if (success)
			response.setStatus(Status.SUCCESS_OK);
		else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		response.setEntity(message, MediaType.TEXT_PLAIN);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		String action = (String) request.getAttributes().get("action");
		if ("restore".equals(action)) {
			try {
				handleRestore(entity, request, response, session);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		else if ("delete".equals(action)) {
			try {
				handleDelete(entity, request, response, session);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		else if ("deleteall".equals(action))
			handleDeleteAll(request, response, session);
	}
}
