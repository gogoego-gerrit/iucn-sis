package org.iucn.sis.server.integrity;

import org.iucn.sis.server.crossport.export.StructureLoader;
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
public class QBLookupListResource extends DBResource {

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

		final Document struct = StructureLoader.loadPostgres();
		final ElementCollection nodes = new ElementCollection(struct
				.getDocumentElement().getElementsByTagName("table"));
		for (Element node : nodes) {
			String table = node.getAttribute("name");
			final ElementCollection cols = new ElementCollection(node
					.getElementsByTagName("column"));
			for (Element col : cols) {
				if (!BaseDocumentUtils.impl.getAttribute(col, "lookup").equals(
						"")) {
					final Element el = document.createElement("column");
					el.setAttribute("canonicalName", table + "."
							+ col.getAttribute("name"));

					final Element child = document.createElement("lookup");
					child.setAttribute("table", col.getAttribute("lookup"));
					child.setAttribute("keyColumn", "index");
					child.setAttribute("valueColumn", "description");

					el.appendChild(child);
					root.appendChild(el);
				}
			}
		}

		/*
		 * Explicit
		 */
		final Element region = document.createElement("column");
		{
			region.setAttribute("canonicalName", "RegionInformation.region_id");
			final Element child = document.createElement("lookup");
			child.setAttribute("table", "RegionLookup");
			child.setAttribute("keyColumn", "region_id");
			child.setAttribute("valueColumn", "region_name");
			region.appendChild(child);
		}
		root.appendChild(region);

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
