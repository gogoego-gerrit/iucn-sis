package org.iucn.sis.server.restlets.taxa;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.CommonNameIO;
import org.iucn.sis.server.api.io.IsoLanguageIO;
import org.iucn.sis.server.api.io.NoteIO;
import org.iucn.sis.server.api.io.ReferenceIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class CommonNameRestlet extends BaseServiceRestlet {

	public CommonNameRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/taxon/{taxon_id}/commonname/{id}/reference");
		paths.add("/taxon/{taxon_id}/commonname/{id}");
		paths.add("/taxon/{taxon_id}/commonname");
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		if (request.getResourceRef().getPath().contains("reference"))
			addOrRemoveReference(entity, request, response, session);
		else if ("primary".equals(request.getAttributes().get("id")))
			setPrimaryCommonName(entity, request, response, session);
		else {
			addOrEditCommonName(entity, request, response, session);
		}
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		//Adding a new common name
		addOrEditCommonName(entity, request, response, session);
	}
	
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		try {
			new CommonNameIO(session).delete(getCommonNameID(request), getUser(request, session));
		} catch (TaxomaticException e) {
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	private void addOrRemoveReference(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final CommonNameIO io = new CommonNameIO(session);
		final ReferenceIO refIO = new ReferenceIO(session);
		
		final CommonName commonName = io.get(getCommonNameID(request));
		if (commonName == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No common name exists with that ID");
		
		final NativeDocument newDoc = getEntityAsNativeDocument(entity);
		
		final StringBuilder log = new StringBuilder();
		final NativeNodeList list = newDoc.getDocumentElement().getElementsByTagName("action");
		for (int i = 0; i < list.getLength(); i++) {
			NativeElement element = list.elementAt(i);
			Integer refID = Integer.valueOf(element.getAttribute("id"));
			String action = element.getTextContent();
			if ("add".equals(action)) {
				refIO.addReference(commonName, refID);
				log.append("<li>Added reference " + refID + "</li>");
			} 
			else if ("remove".equals(action)) {
				if (refIO.removeReference(commonName, refID))
					log.append("<li>Removed reference " + refID + "</li>");
				else
					log.append("<li>Reference " + refID + " not attached</li>");
			}
		}
		
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity("<ul>" + log + "</ul>", MediaType.TEXT_XML);
	}
	
	private void addOrEditCommonName(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final NativeDocument newDoc = getEntityAsNativeDocument(entity);
		final CommonNameIO io = new CommonNameIO(session);
		
		TaxonIO taxonIO = new TaxonIO(session);
		NoteIO noteIO = new NoteIO(session);
		IsoLanguageIO isoLanguageIO = new IsoLanguageIO(session);
		
		Taxon taxon = taxonIO.getTaxon(getTaxonID(request));
		
		CommonName source = CommonName.fromXML(newDoc.getDocumentElement());
		source.setIso(isoLanguageIO.getIsoLanguageByCode(source.getIsoCode()));
		if (source.getId() == 0) {
			try {
				io.add(taxon, source, getUser(request, session));
			} catch (TaxomaticException e) {
				throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
			}
			
			session.flush();
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(source.getId() + "", MediaType.TEXT_PLAIN);
		} else {
			IsoLanguageIO isoIO = new IsoLanguageIO(session);
			
			CommonName target = io.get(source.getId());
			if (target == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			
			target.setName(source.getName());
			target.setIso(isoIO.getIsoLanguageByCode(source.getIsoCode()));
			target.setChangeReason(source.getChangeReason());
			target.setValidated(source.getValidated());
			
			try {
				io.update(target);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(target.getId() + "", MediaType.TEXT_PLAIN);
		}
	}
	
	private void setPrimaryCommonName(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final NativeDocument newDoc = getEntityAsNativeDocument(entity);
		final CommonName commonName = CommonName.fromXML(newDoc.getDocumentElement());
		
		final CommonNameIO io = new CommonNameIO(session);
		io.setPrimary(commonName.getId());
	}
	
	protected Integer getCommonNameID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String) request.getAttributes().get("id"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
	}
	
	protected Integer getTaxonID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String) request.getAttributes().get("taxon_id"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
	}
	
}
