package org.iucn.sis.server.users.resources;

import java.util.ArrayList;

import org.iucn.sis.server.users.container.UserManagementApplication;
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
import org.w3c.dom.Node;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;

/**
 * CustomFieldManager.java
 * 
 * Resource that handes the adding and removal of custom field enumerations.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class CustomFieldManager extends Resource {

	private final ExecutionContext ec;

	public CustomFieldManager(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		ec = UserManagementApplication.getFromContext(context).getExecutionContext();

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final Integer id;
		try {
			id = Integer.parseInt((String) getRequest().getAttributes().get("id"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}

		final Document put;
		try {
			put = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}

		final Row row = new Row();

		final ArrayList<String> options = new ArrayList<String>();
		final NodeCollection nodes = new NodeCollection(put.getDocumentElement().getChildNodes());

		for (Node node : nodes) {
			final String name = node.getNodeName();
			if ("name".equals(name) || "type".equals(name) || "required".equals(name))
				row.add(new CString(name, node.getTextContent()));
			else if ("option".equals(name)) {
				options.add(node.getTextContent());
			}
		}

		if (!options.isEmpty()) {
			String value = "";
			for (int i = 0; i < options.size(); i++)
				value += options.get(i) + ((i + 1) < options.size() ? "::" : "");
			row.add(new CString("options", value));
		}

		final UpdateQuery query = new UpdateQuery();
		query.setRow(row);
		query.setTable("customfield");
		query.constrain(new CanonicalColumnName("customfield", "id"), QConstraint.CT_EQUALS, id);

		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		getResponse().setStatus(Status.SUCCESS_OK);
	}

	/**
	 * Because there's no GET implemented, this needs to be shortcutted.
	 * 
	 */
	@Override
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	@Override
	public void removeRepresentations() throws ResourceException {
		final Integer id;
		try {
			id = Integer.parseInt((String) getRequest().getAttributes().get("id"));
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}

		final DeleteQuery query = new DeleteQuery();
		query.setTable("customfield");
		query.constrain(new CanonicalColumnName("customfield", "id"), QConstraint.CT_EQUALS, id);

		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		getResponse().setStatus(Status.SUCCESS_OK);
	}

	/*
	 * <root> <{field}>{value}</{field}> </root>
	 */
	@Override
	public void storeRepresentation(Representation entity) throws ResourceException {
		final Document put;
		try {
			put = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}

		final Row row = new Row();
		final Integer id;

		try {
			id = Integer.valueOf((int) RowID.get(ec, "customfield", "id"));
			row.add(new CInteger("id", id));
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		final ArrayList<String> options = new ArrayList<String>();
		final NodeCollection nodes = new NodeCollection(put.getDocumentElement().getChildNodes());
		for (Node node : nodes) {
			final String name = node.getNodeName();
			if ("name".equals(name)) {
				SelectQuery query = new SelectQuery();
				query.select("customfield", "id");
				query.constrain(new CanonicalColumnName("customfield", "name"), QConstraint.CT_EQUALS, node
						.getTextContent());

				final Row.Loader rl = new Row.Loader();

				try {
					ec.doQuery(query, rl);
				} catch (DBException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}

				if (rl.getRow() == null)
					row.add(new CString(name, node.getTextContent()));
				else
					throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "This field already exists.");

			} else if ("type".equals(name) || "required".equals(name))
				row.add(new CString(name, node.getTextContent()));
			else if ("option".equals(name)) {
				options.add(node.getTextContent());
			}
		}

		if (!options.isEmpty()) {
			String value = "";
			for (int i = 0; i < options.size(); i++)
				value += options.get(i) + ((i + 1) < options.size() ? "::" : "");
			row.add(new CString("options", value));
		}

		final InsertQuery query = new InsertQuery();
		query.setRow(row);
		query.setTable("customfield");

		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		final Document results = BaseDocumentUtils.impl.newDocument();
		final Element root = results.createElement("root");

		final Element field = results.createElement("customfield");
		field.setAttribute("id", id.toString());
		root.appendChild(field);

		results.appendChild(root);

		getResponse().setStatus(Status.SUCCESS_CREATED);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, results));
	}
}
