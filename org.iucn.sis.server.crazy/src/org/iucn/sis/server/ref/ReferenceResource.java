package org.iucn.sis.server.ref;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.server.utils.SelectCountDBProcessor;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.StringLiteral;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

public class ReferenceResource extends Resource {

	public ReferenceResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void removeRepresentations() throws ResourceException {
		try {
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
			String refID = (String) getRequest().getAttributes().get("refid");

			String sql = "SELECT COUNT(*) AS rowcount FROM assessment_reference where ref_id="
					+ ec.formatLiteral(new StringLiteral(refID)) + ";";
			SelectCountDBProcessor proc = new SelectCountDBProcessor();
			ec.doQuery(sql, proc);

			if (proc.getCount() == 0) {
				DeleteQuery dq = new DeleteQuery("bibliography", "Bib_hash", getRequest().getAttributes().get("refid"));
				ec.doUpdate(dq);
				getResponse().setStatus(Status.SUCCESS_OK);
			} else
				getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);

		} catch (final DBException dbx) {
			dbx.printStackTrace();
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (final NamingException nx) {
			nx.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Override
	public Representation represent(final Variant variant) {
		try {
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			final SelectQuery sq = new SelectQuery();
			sq.select("bibliography", "*");
			sq.constrain(new CanonicalColumnName("bibliography", "Bib_hash"), QConstraint.CT_EQUALS, getRequest()
					.getAttributes().get("refid"));
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element rootEl = doc.createElement("references");
			doc.appendChild(rootEl);

			ec.doQuery(sq, new ReferenceRowProcessor(doc, rootEl, ReferenceLabels.loadFrom(getContext())));
			return new DomRepresentation(MediaType.TEXT_XML, doc);
		} catch (final DBException dbx) {
			dbx.printStackTrace();
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("No such reference", MediaType.TEXT_PLAIN);
		} catch (final NamingException nx) {
			nx.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation("Reference database not available", MediaType.TEXT_PLAIN);
		} catch (final ParserConfigurationException px) {
			px.printStackTrace();
			throw new RuntimeException("XML Parser not properly configured", px);
		}
	}
}
