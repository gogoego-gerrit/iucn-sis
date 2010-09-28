package org.iucn.sis.server.extensions.references;

import java.io.IOException;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.server.extensions.references.ReferenceLabels.LabelMappings;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

public class SubmissionResource extends Resource {

	public SubmissionResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final DomRepresentation dom = new DomRepresentation(entity);
		try {
			final Document responseDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element rootEl = responseDoc.createElement("references");
			responseDoc.appendChild(rootEl);
			final ExecutionContext ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
			final Document doc = dom.getDocument();
			final NodeList references = doc.getElementsByTagName("reference");
			for (int i = 0; i < references.getLength(); i++) {
				final Element reference = (Element) references.item(i);
				final String id = reference.getAttribute("id");
				final Row r = ec.getRow("bibliography");
				String type = reference.getAttribute("type");
				LabelMappings lm = ReferenceLabels.getInstance().get(type);
				r.get("Publication_Type").setObject(type);

				final NodeList fields = reference.getElementsByTagName("field");
				for (int j = 0; j < fields.getLength(); j++) {
					final Element field = (Element) fields.item(j);
					String name = field.getAttribute("name");
					// String capitalized = lm.getCapitalized(name);
					// if(capitalized != null) name = capitalized;

					String normalized = LabelMappings.normalize(name);
					if (normalized != null)
						name = normalized;

					final String value = field.getTextContent();

					Column c = r.get(name);

					if (c != null) {
						if (value == null || value.equals(""))
							c.setObject(null);
						else
							c.setString(value);
					}
				}

				String newid = r.getMD5Hash();
				if (id == null || !newid.equals(id)) {
					// insert new reference
					r.get("Bib_hash").setObject(newid);
					InsertQuery query = new InsertQuery("bibliography", r);
					try {
						ec.doUpdate(query);
					} catch (DBException dbx) {
						dbx.printStackTrace();

						// TODO: COLLISION DETECTED -- NEED TO DO AN UPDATE FOR
						// TRANSIENT FIELDS, BUT SHOULD DETECT TO ENSURE IT'S
						// BODILY
						// THE SAME REFERENCE, AND NOT AN ACTUAL HASH COLLISION
					}
				}
				// re-query, and glue the canonical representation of this ref
				// into the response document.
				SelectQuery sq = new SelectQuery();
				sq.select("bibliography", "*");
				sq.constrain(new CanonicalColumnName("bibliography", "Bib_hash"), QConstraint.CT_EQUALS, newid);
				ec.doQuery(sq, new ReferenceRowProcessor(responseDoc, rootEl, ReferenceLabels.loadFrom(getContext())));
			}

			getResponse().setStatus(Status.SUCCESS_OK);
			getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, responseDoc));
		} catch (final IOException iox) {
			iox.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (final DBException dbx) {
			dbx.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (final ParserConfigurationException pcx) {
			pcx.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (final NamingException nx) {
			nx.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
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
