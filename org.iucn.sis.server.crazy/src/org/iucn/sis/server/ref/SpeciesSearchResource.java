package org.iucn.sis.server.ref;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.SysDebugger;

public class SpeciesSearchResource extends Resource {

	public SpeciesSearchResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(final Variant variant) {
		final Form f = getRequest().getResourceRef().getQueryAsForm();
		try {
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			final SelectQuery sq = new SelectQuery();
			sq.select("bibliography", "*");
			sq.join("bibliography_link", new QRelationConstraint(new CanonicalColumnName("bibliography", "Bib_code"),
					new CanonicalColumnName("bibliography_link", "Bibliography_number")));
			sq.join("systematics", new QRelationConstraint(new CanonicalColumnName("systematics", "Sp_code"),
					new CanonicalColumnName("bibliography_link", "Sp_code")));
			for (final Parameter p : f)
				sq.constrain(new CanonicalColumnName("systematics", p.getName()), QConstraint.CT_EQUALS, p.getValue());
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			@SuppressWarnings("unchecked")
			final Element rootEl = doc.createElement("references");
			doc.appendChild(rootEl);
			SysDebugger.getInstance().println(sq.getSQL(ec.getDBSession()));
			ec.doQuery(sq, new ReferenceRowProcessor(doc, rootEl, ReferenceLabels.loadFrom(getContext())));
			return new DomRepresentation(MediaType.TEXT_XML, doc);
		} catch (final DBException dbx) {
			dbx.printStackTrace();
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("No matching references found", MediaType.TEXT_PLAIN);
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
