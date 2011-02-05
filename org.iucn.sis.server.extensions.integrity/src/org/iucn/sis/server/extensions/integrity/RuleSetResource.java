package org.iucn.sis.server.extensions.integrity;

import org.hibernate.Session;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * RuleSetResource.java
 * 
 * Load and save rulesets
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
@SuppressWarnings("deprecation")
public class RuleSetResource extends BaseIntegrityResource {

	private final String rule;

	public RuleSetResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		rule = (String) request.getAttributes().get("rule");

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		if (rule == null) {
			VFSPathToken[] tokens;
			try {
				tokens = vfs.list(ROOT_PATH);
			} catch (NotFoundException e) {
				tokens = new VFSPathToken[0];
			}

			final Document document = BaseDocumentUtils.impl.newDocument();
			final Element root = document.createElement("root");
			for (VFSPathToken token : tokens) {
				final Element el = BaseDocumentUtils.impl
						.createElementWithText(document, "uri", ROOT_PATH
								.child(token).toString());
				el.setAttribute("name", token.toString());
				root.appendChild(el);
			}

			document.appendChild(root);

			return new DomRepresentation(variant.getMediaType(), document);
		} else {
			try {
				return new InputRepresentation(vfs.getInputStream(ROOT_PATH
						.child(new VFSPathToken(rule))), variant.getMediaType());
			} catch (NotFoundException e) {
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
			}
		}
	}
	
	@Override
	public void acceptRepresentation(Representation entity, Session session) throws ResourceException {
		writeRule(entity, true);
		if (getResponse().getStatus().isSuccess()) {
			final DeleteQuery query = new DeleteQuery();
			query.setTable("assessment_integrity_status");
			query.constrain(new CanonicalColumnName("assessment_integrity_status", "rule"), QConstraint.CT_EQUALS, rule);
			
			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}

	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	@Override
	public void storeRepresentation(Representation entity, Session session) throws ResourceException {
		writeRule(entity, false);
	}
	
	private void writeRule(Representation entity, boolean allowOverwrite) throws ResourceException {
		if (rule == null)
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final VFSPath uri = ROOT_PATH.child(new VFSPathToken(rule));
		
		if (!allowOverwrite && vfs.exists(uri))
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);

		if (DocumentUtils.writeVFSFile(uri.toString(), vfs, true, document))
			getResponse().setStatus(allowOverwrite ? Status.SUCCESS_OK : Status.SUCCESS_CREATED);
		else
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	}

	@Override
	public void removeRepresentations(Session session) throws ResourceException {
		if (rule == null)
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

		try {
			vfs.delete(ROOT_PATH.child(new VFSPathToken(rule)));
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (ConflictException e) {
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
		}
	}

}
