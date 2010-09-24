package org.iucn.sis.server.extensions.integrity;

import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.utils.StructureLoader;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.db.DBException;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.restlet.DBResource;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;

/**
 * QBLookupListResource.java
 * 
 * QueryBuilder support for lookup tables.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class QBLookupListResource extends IntegrityDBResource {

	public QBLookupListResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		setModifiable(true);

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	/**
	 * <root> <column canonicalName="table.column"> <lookup
	 * table="..."keyColumn="..." valueColumn="..." /> </column> </root>
	 */
	public Representation represent(Variant variant) throws ResourceException {
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("root");
		
		final List<String> tables;
		try {
			tables = ec.getDBSession().listTables(ec);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		for (String table : tables) {
			if (table.toLowerCase().endsWith("lookup") && table.contains("_")) {
				String[] split = table.split("_");
				String column = split[1].toLowerCase().replaceAll("lookup", "");
				
				final Element el = document.createElement("column");
				el.setAttribute("canonicalName", split[0] + "."
						+ column);

				final Element child = document.createElement("lookup");
				child.setAttribute("table", table);
				child.setAttribute("keyColumn", "id");
				child.setAttribute("valueColumn", "label");

				el.appendChild(child);
				
				root.appendChild(el);
			}
		}

		document.appendChild(root);

		return new DomRepresentation(variant.getMediaType(), document);
	}

	public void acceptRepresentation(Representation entity)
			throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(
					Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}

		final NodeList list = document.getDocumentElement()
				.getElementsByTagName("lookup");
		if (list == null || list.getLength() < 1)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		final Element el = (Element) list.item(0);

		final SelectQuery query = new SelectQuery();
		query.select(el.getAttribute("table"), el.getAttribute("keyColumn"));
		query.select(el.getAttribute("table"), el.getAttribute("valueColumn"));

		getResponse().setEntity(getRowsAsRepresentation(query));
		getResponse().setStatus(Status.SUCCESS_OK);
	}
}
