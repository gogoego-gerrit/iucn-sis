/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.gogoego.server.lib.app.tags.resources.admin;

import org.gogoego.api.utils.DocumentUtils;
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
import org.w3c.dom.Node;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.gogoego.server.lib.app.tags.resources.BaseTagResource;
import com.solertium.util.NodeCollection;

/**
 * AvailableRegistryResource.java
 * 
 * @author carl.scott
 * 
 */
public class AvailableRegistryResource extends BaseTagResource {

	private final String protocol;

	public AvailableRegistryResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		protocol = (String) request.getAttributes().get("protocol");
	}

	public Representation represent(Variant variant) throws ResourceException {
		final String filter = convertTable(TABLE_GROUPFILTERS);

		final SelectQuery query = new SelectQuery();
		query.select(filter, "id");
		query.select(filter, "key");
		if (protocol != null)
			query.constrain(new CanonicalColumnName(filter, "protocol"), QConstraint.CT_EQUALS, protocol);

		final Row.Set rs = new Row.Set();

		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		return new DomRepresentation(variant.getMediaType(), QueryUtils.writeDocumentFromRowSet(rs.getSet()));
	}

	/*
	 * <root> <key> ... </key> </root>
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		if (protocol == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		final NodeCollection nodes = new NodeCollection(getDocument(entity).getDocumentElement().getChildNodes());

		final String table = convertTable(TABLE_GROUPFILTERS);

		final Document document = DocumentUtils.impl.newDocument();
		document.appendChild(document.createElement("root"));

		for (Node node : nodes) {
			if (node.getNodeName().equals("key")) {
				final String text = node.getTextContent();

				// Not sure if you can make strings unique so i'll check...
				{
					final SelectQuery query = new SelectQuery();
					query.select(table, "id");
					query.constrain(new CanonicalColumnName(table, "key"), QConstraint.CT_EQUALS, text);

					Row.Loader rl = new Row.Loader();
					try {
						ec.doQuery(query, rl);
					} catch (DBException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Query failed", e);
					}

					if (rl.getRow() != null) {
						getResponse().setEntity(
								new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
										.createErrorDocument("Duplicate entry for " + text)));
						throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "This key already exists.");
					}
				}

				final Integer id;
				final Row template;
				try {
					template = ec.getRow(table);
					template.get("id").setObject(id = Integer.valueOf((int) getRowID().get(ec, table, "id")));
					template.get("key").setObject(text);
					template.get("protocol").setObject(protocol);
				} catch (DBException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not get row template.");
				}

				final InsertQuery query = new InsertQuery();
				query.setTable(table);
				query.setRow(template);

				final Element newChild = document.createElement("key");
				newChild.setAttribute("name", text);
				newChild.setAttribute("id", id.toString());
				try {
					ec.doUpdate(query);
					newChild.setAttribute("success", "true");
				} catch (DBException e) {
					newChild.setAttribute("success", "false");
				}

				document.getDocumentElement().appendChild(newChild);
			}
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, document));
	}

}
