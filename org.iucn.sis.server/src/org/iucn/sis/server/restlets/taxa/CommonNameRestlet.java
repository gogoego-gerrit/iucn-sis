package org.iucn.sis.server.restlets.taxa;

import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.CommonNameDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class CommonNameRestlet extends BaseServiceRestlet {

	public CommonNameRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/taxon/{taxon_id}/commonname/{id}/note/{note_id}");
		paths.add("/taxon/{taxon_id}/commonname/{id}/note");
		paths.add("/taxon/{taxon_id}/commonname/{id}/reference");
		paths.add("/taxon/{taxon_id}/commonname/{id}");
		paths.add("/taxon/{taxon_id}/commonname");
	}

	protected void addOrRemoveReference(Representation entity, Request request, Response response) throws ResourceException {
		NativeDocument newDoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		try {
			newDoc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		Integer id;
		try {
			id = Integer.valueOf((String) request.getAttributes().get("id"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		
		try {
			CommonName commonName = SIS.get().getManager().getObject(CommonName.class, id);
			NativeNodeList list = newDoc.getDocumentElement().getElementsByTagName("action");
			for (int i = 0; i < list.getLength(); i++) {
				NativeElement element = list.elementAt(i);
				Integer refID = Integer.valueOf(element.getAttribute("id"));
				String action = element.getTextContent();
				if (action.equalsIgnoreCase("add")) {
					Reference ref = SIS.get().getManager().getObject(Reference.class, id);
					commonName.getReference().add(ref);
				} else {
					Reference toDelete = null;
					for (Reference ref : commonName.getReference()) {
						if (ref.getId() == refID) {
							toDelete = ref;
							break;
						}
					}
					commonName.getReference().remove(toDelete);
					
					SISPersistentManager.instance().mergeObject(commonName);
					commonName.getTaxon().toXML();
					response.setStatus(Status.SUCCESS_OK);
					response.setEntity(commonName.getId() + "", MediaType.TEXT_PLAIN);
				}
			}
		} catch (PersistentException e) {
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
		final Integer id;
		try {
			id = Integer.valueOf((String) request.getAttributes().get("id"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		
		final NativeDocument ndoc = new JavaNativeDocument();
		try {
			ndoc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		Notes note = Notes.fromXML(ndoc.getDocumentElement());
		try {
			CommonName commonName = SIS.get().getManager()
					.getObject(CommonName.class, Integer.valueOf(id));
			note.setCommonName(commonName);
			note = SIS.get().getManager().mergeObject(note);
			commonName.getNotes().add(note);
			commonName.getTaxon().toXML();
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(note.getId() + "", MediaType.TEXT_PLAIN);
		} catch (PersistentException e) {
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	protected void addOrEditCommonName(Representation entity, Request request, Response response) throws ResourceException {
		NativeDocument newDoc = new JavaNativeDocument();
		try {
			newDoc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		String taxonID = (String) request.getAttributes().get("taxon_id");
		
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.parseInt(taxonID));
		
		CommonName commonName = CommonName.fromXML(newDoc.getDocumentElement());
		commonName.setIso(SIS.get().getIsoLanguageIO().getIsoLanguageByCode(commonName.getIsoCode()));
		Set<Notes> notes = new HashSet<Notes>();
		for (Notes note : commonName.getNotes()) {
			if (note.getId() != 0)
				notes.add(SIS.get().getNoteIO().get(note.getId()));
		}
		commonName.setNotes(notes);
		if (commonName.getId() == 0) {
			taxon.getCommonNames().add(commonName);
			commonName.setTaxon(taxon);
			
			try {
				SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request));
			} catch (TaxomaticException e) {
				throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
			}
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(commonName.getId() + "", MediaType.TEXT_PLAIN);

		} else {
			commonName.setTaxon(taxon);
			try {
				SISPersistentManager.instance().mergeObject(commonName);
				taxon.getCommonNames().add(commonName);
				taxon.toXML();
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(commonName.getId() + "", MediaType.TEXT_PLAIN);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}

	protected void deleteNote(Request request, Response response) {
		Integer id = Integer.parseInt((String) request.getAttributes().get("id"));
		Integer noteID = Integer.parseInt((String) request.getAttributes().get("note_id"));
		try {
			CommonName commonName = SIS.get().getManager().getObject(CommonName.class, id);
			Notes noteToDelete = null;
			for (Notes note : commonName.getNotes()) {
				if (note.getId() == noteID.intValue()) {
					noteToDelete = note;
					break;
				}
			}

			if (noteToDelete != null) {
				commonName.getNotes().remove(noteToDelete);
				commonName.getTaxon().toXML();
				Debug.println("this is the taxon xml " + commonName.getTaxon().toXML());
				if (SIS.get().getNoteIO().delete(noteToDelete)) {
					response.setStatus(Status.SUCCESS_OK);
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}

			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (PersistentException e) {
			Debug.println(e);
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	protected void deleteCommonName(Request request, Response response) throws ResourceException {
		Integer taxonID = Integer.parseInt((String) request.getAttributes().get("taxon_id"));
		Integer id = Integer.parseInt((String) request.getAttributes().get("id"));

		Taxon taxon = SIS.get().getTaxonIO().getTaxon(taxonID);
		CommonName toDelete = null;
		for (CommonName syn : taxon.getCommonNames()) {
			if (syn.getId() == id) {
				toDelete = syn;
				break;
			}
		}
		if (toDelete == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			
		taxon.getCommonNames().remove(toDelete);
		taxon.toXML();
		
		try {
			SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request));
		} catch (TaxomaticException e) {
			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
		
		try {
			CommonNameDAO.delete(toDelete);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public void handleDelete(Request request, Response response) throws ResourceException {
		if (request.getResourceRef().getPath().contains("note")) {
			deleteNote(request, response);
		} else
			deleteCommonName(request, response);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		if (request.getResourceRef().getPath().contains("reference")) {
			addOrRemoveReference(entity, request, response);
		} else
			addOrEditCommonName(entity, request, response);
	}

}
