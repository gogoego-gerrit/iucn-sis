/**
 *
 */
package com.solertium.gogoego.server.lib.app.tags.resources.admin;

import java.util.ArrayList;
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

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.gogoego.server.lib.app.tags.resources.global.TagGroupResource;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;

/**
 * WritableTagGroupResource.java
 * 
 * @author carl.scott
 * 
 */
public class WritableTagGroupResource extends TagGroupResource {

	public WritableTagGroupResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
	}

	public void removeRepresentations() throws ResourceException {
		if (groupID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		final ArrayList<DeleteQuery> queries = new ArrayList<DeleteQuery>();

		{
			final String table = convertTable(TABLE_GROUPS);
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, groupID);
			queries.add(query);
		}
		{
			final String table = convertTable(TABLE_GROUPFILTERGROUPS);
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new CanonicalColumnName(table, "groupid"), QConstraint.CT_EQUALS, groupID);
			queries.add(query);
		}
		{
			final String table = convertTable(TABLE_GROUPTAGS);
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new CanonicalColumnName(table, "groupid"), QConstraint.CT_EQUALS, groupID);
			queries.add(query);
		}
		{
			final String table = convertTable(TABLE_GROUPFILTERS);
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new CanonicalColumnName(table, "groupid"), QConstraint.CT_EQUALS, groupID);
			queries.add(query);
		}

		for (DeleteQuery query : queries) {
			try {
				ec.doUpdate(query);
			} catch (DBException unlikely) {
				TrivialExceptionHandler.ignore(this, unlikely);
			}
		}

		getResponse().setStatus(Status.SUCCESS_OK);
	}

	/*
	 * <root> <group id="group"> {name} </group> </root>
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final NodeCollection nodes = new NodeCollection(getDocument(entity).getDocumentElement().getChildNodes());

		final Document document = DocumentUtils.impl.newDocument();
		document.appendChild(document.createElement("root"));

		final String table = convertTable(TABLE_GROUPS);

		final Row template;
		try {
			template = ec.getRow(table);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not load template row.", e);
		}

		for (Node node : nodes) {
			if (node.getNodeName().equals("group")) {
				final Integer id;
				try {
					id = Integer.valueOf(DocumentUtils.impl.getAttribute(node, "id"));
				} catch (NumberFormatException e) {
					Element failure = document.createElement("group");
					failure.setAttribute("id", DocumentUtils.impl.getAttribute(node, "id"));
					failure.setAttribute("success", "false");
					failure.setTextContent(e.getMessage());
					document.getDocumentElement().appendChild(failure);
					continue;
				}

				template.get("id").setObject(id);
				template.get("name").setObject(node.getTextContent());

				final Element status = document.createElement("group");
				status.setAttribute("id", id.toString());

				final UpdateQuery query = new UpdateQuery();
				query.setTable(table);
				query.setRow(template);
				query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);

				try {
					ec.doUpdate(query);
					status.setAttribute("success", "true");
				} catch (DBException e) {
					status.setAttribute("success", "false");
					status.setTextContent(e.getMessage());
				}

				document.getDocumentElement().appendChild(status);
			}
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, document));
	}

	public void storeRepresentation(Representation entity) throws ResourceException {
		Document document = getDocument(entity);
		if (groupID == null) {
			createNewTagGroup(document);
		} else {
			addTagsToTagGroup(document);
		}
	}

	/*
	 * <root> <group> ... </group> </root>
	 */
	public void createNewTagGroup(Document document) throws ResourceException {
		final HashMap<Integer, String> newGroups = new HashMap<Integer, String>();

		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		final String table = convertTable(TABLE_GROUPS);

		for (Node node : nodes) {
			if (node.getNodeName().equals("group"))
				try {
					final String name = node.getTextContent();

					final SelectQuery query = new SelectQuery();
					query.select(table, "id");
					query.constrain(new CanonicalColumnName(table, "name"), QConstraint.CT_EQUALS, name);

					final Row.Loader rl = new Row.Loader();

					ec.doQuery(query, rl);

					if (rl.getRow() == null)
						newGroups.put(Integer.valueOf((int) getRowID().get(ec, table, "id")), name);
					else {
						getResponse().setEntity(
								new StringRepresentation("<root><tag>" + name + "</tag></root>", MediaType.TEXT_XML));
						throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
					}
				} catch (DBException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not generate ID for group");
				}
		}

		final Document ret = DocumentUtils.impl.newDocument();
		ret.appendChild(ret.createElement("root"));

		final Iterator<Map.Entry<Integer, String>> iterator = newGroups.entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<Integer, String> entry = iterator.next();
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
			template.get("name").setObject(entry.getValue());

			query.setRow(template);

			Element entryNode = ret.createElement("group");
			entryNode.setAttribute("id", entry.getKey() + "");
			entryNode.setAttribute("name", entry.getValue());
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

	/*
	 * <root> <tag> ... </tag> </root>
	 */
	public void addTagsToTagGroup(Document document) throws ResourceException {
		final String table = convertTable(TABLE_GROUPTAGS);

		final Map<Integer, Integer> existingTags = new HashMap<Integer, Integer>();
		{
			final SelectQuery query = new SelectQuery();
			query.select(table, "tagid");
			query.select(table, "id");
			query.constrain(new CanonicalColumnName(table, "groupid"), QConstraint.CT_EQUALS, groupID);

			final Row.Set rs = new Row.Set();

			try {
				ec.doQuery(query, rs);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}

			for (Row row : rs.getSet())
				existingTags.put(row.get("tagid").getInteger(), row.get("id").getInteger());
		}
		
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		final Document ret = DocumentUtils.impl.newDocument();
		ret.appendChild(ret.createElement("root"));

		for (Node node : nodes) {
			if (node.getNodeName().equals("tag")) {
				final Integer tagID = new Integer(node.getTextContent());
				if (existingTags.containsKey(tagID)) {
					final Element entryNode = ret.createElement("tag");
					entryNode.setAttribute("tagid", node.getTextContent());
					entryNode.setAttribute("success", "false");
					entryNode.setTextContent("duplicate");
					ret.getDocumentElement().appendChild(entryNode);
					existingTags.remove(tagID);
					continue;
				} else
					existingTags.remove(tagID);

				final InsertQuery query = new InsertQuery();
				query.setTable(table);

				final Row template;
				try {
					template = ec.getRow(table);
				} catch (DBException e) {
					getFailureDocument(e);
					return;
				}

				final Integer rowID;

				try {
					template.get("id").setObject(rowID = Integer.valueOf((int) getRowID().get(ec, table, "id")));
					template.get("tagid").setObject(tagID);
					template.get("groupid").setObject(new Integer(groupID));
				} catch (DBException e) {
					getFailureDocument(e);
					return;
				} catch (NumberFormatException e) {
					continue;
				}

				query.setRow(template);

				final Element entryNode = ret.createElement("tag");
				entryNode.setAttribute("id", rowID + "");
				entryNode.setAttribute("tagid", node.getTextContent());

				try {
					ec.doUpdate(query);
					entryNode.setAttribute("success", "true");
				} catch (DBException e) {
					entryNode.setAttribute("success", "false");
					entryNode.setTextContent(e.getMessage());
				}

				ret.getDocumentElement().appendChild(entryNode);
			}
		}

		for (Integer rowID : existingTags.values()) {
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, rowID);

			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, ret));
	}

}
