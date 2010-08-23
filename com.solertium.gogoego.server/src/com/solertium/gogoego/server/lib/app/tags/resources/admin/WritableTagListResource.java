/**
 *
 */
package com.solertium.gogoego.server.lib.app.tags.resources.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.gogoego.server.lib.app.tags.resources.global.TagListResource;
import com.solertium.util.NodeCollection;

/**
 * WritableTagListResource.java
 * 
 * @author carl.scott
 * 
 */
public class WritableTagListResource extends TagListResource {

	public WritableTagListResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
	}

	/*
	 * <root> <tag id="..."> ... </tag> </root>
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		Document document = getDocument(entity);

		final HashMap<Integer, Node> existingTags = new HashMap<Integer, Node>();

		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		final String table = convertTable(TABLE_TAGS);

		for (Node node : nodes) {
			if (node.getNodeName().equals("tag"))
				try {
					final Integer id = Integer.parseInt(DocumentUtils.impl.getAttribute(node, "id"));
					existingTags.put(id, node);
				} catch (NumberFormatException e) {
					throw new ResourceException(e);
				}
		}

		final Document ret = DocumentUtils.impl.newDocument();
		ret.appendChild(ret.createElement("root"));

		final Iterator<Map.Entry<Integer, Node>> iterator = existingTags.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Node> entry = iterator.next();

			UpdateQuery query = new UpdateQuery();
			query.setTable(table);

			final Row template = new Row();
			template.add(new CString("name", entry.getValue().getTextContent()));
			template.add(new CString("attributes", DocumentUtils.impl.getAttribute(entry.getValue(), "attributes")));

			query.setRow(template);

			query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, entry.getKey());

			final Element result = ret.createElement("tag");
			result.setAttribute("id", entry.getKey().toString());
			result.setAttribute("name", entry.getValue().getTextContent());
			try {
				ec.doUpdate(query);
				result.setAttribute("success", "true");
			} catch (DBException e) {
				result.setAttribute("success", "false");
				result.setTextContent(e.getMessage());
			}

			ret.getDocumentElement().appendChild(result);
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, ret));
	}

	/*
	 * <root> <tag id="" /> </root>
	 */
	public void removeRepresentations() throws ResourceException {
		final Integer id;
		try {
			id = new Integer(getRequest().getResourceRef().getLastSegment());
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}

		final String table = convertTable(TABLE_TAGS);

		final DeleteQuery query = new DeleteQuery();
		query.setTable(table);
		query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);

		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}

		getResponse().setStatus(Status.SUCCESS_OK);
	}

	/*
	 * <root> <tag> ... </tag> </root>
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		Document document = getDocument(entity);

		final HashMap<Integer, Node> newTags = new HashMap<Integer, Node>();

		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		final String table = convertTable(TABLE_TAGS);

		for (Node node : nodes) {
			if (node.getNodeName().equals("tag"))
				try {
					final String name = node.getTextContent();

					final SelectQuery query = new SelectQuery();
					query.select(table, "id");
					query.constrain(new CanonicalColumnName(table, "name"), QConstraint.CT_EQUALS, name);

					Row.Loader rl = new Row.Loader();
					ec.doQuery(query, rl);

					if (rl.getRow() == null)
						newTags.put(Integer.valueOf((int) getRowID().get(ec.getDBSession(), convertTable(TABLE_TAGS),
								"id")), node);
					else {
						getResponse().setEntity(
								new StringRepresentation("<root><tag>" + name + "</tag></root>", MediaType.TEXT_XML));
						throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
					}
				} catch (DBException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not generate ID for tag");
				}
		}

		final Document ret = DocumentUtils.impl.newDocument();
		ret.appendChild(ret.createElement("root"));

		final Iterator<Map.Entry<Integer, Node>> iterator = newTags.entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<Integer, Node> entry = iterator.next();
			final InsertQuery query = new InsertQuery();
			query.setTable(table);

			final Row template;
			try {
				template = ec.getRow(table);
			} catch (DBException e) {
				getFailureDocument(e);
				return;
			}

			template.get("id").setObject(entry.getKey());
			template.get("name").setObject(entry.getValue().getTextContent());
			String attrs = DocumentUtils.impl.getAttribute(entry.getValue(), "attributes");
			if (!attrs.equals(""))
				template.get("attributes").setObject(attrs);

			query.setRow(template);

			final Element entryNode = ret.createElement("tag");
			entryNode.setAttribute("id", entry.getKey() + "");
			entryNode.setAttribute("name", entry.getValue().getTextContent());
			try {
				ec.doUpdate(query);
				entryNode.setAttribute("success", "true");
			} catch (DBException e) {
				entryNode.setAttribute("success", "false");
				entryNode.setTextContent(e.getMessage());
			}

			ret.getDocumentElement().appendChild(entryNode);
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, ret));
	}

}
