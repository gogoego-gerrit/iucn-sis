package org.iucn.sis.server.extensions.references;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.TransactionResource;
import org.iucn.sis.shared.api.models.Reference;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class SubmissionResource extends TransactionResource {

	public SubmissionResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public void acceptRepresentation(Representation entity, Session session) throws ResourceException {
		final NativeDocument doc = new JavaNativeDocument();
		try {
			doc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final StringBuilder responseDoc = new StringBuilder();
		responseDoc.append("<references>");
		
		final NativeNodeList nodes = doc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if ("reference".equals(node.getNodeName())) {
				final Reference reference = Reference.fromXML(node, true);

				try {
					SISPersistentManager.instance().saveObject(session, reference);
				} catch (PersistentException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				
				responseDoc.append(reference.toXML());
			}
		}
		
		responseDoc.append("</references>");
		
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new StringRepresentation(responseDoc, MediaType.TEXT_XML));
	}

	@Override
	public boolean allowGet() {
		return false;
	}

	@Override
	public boolean allowHead() {
		return false;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

}
