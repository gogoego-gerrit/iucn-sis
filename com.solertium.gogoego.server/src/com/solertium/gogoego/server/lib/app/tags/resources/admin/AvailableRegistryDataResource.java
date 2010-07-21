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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.gogoego.server.lib.app.tags.resources.BaseTagResource;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * AvailableRegistryDataResource.java
 * 
 * @author carl.scott
 * 
 */
public class AvailableRegistryDataResource extends BaseTagResource {

	private final Integer id;
	private final String protocol;

	public AvailableRegistryDataResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		Integer id;
		try {
			id = Integer.parseInt((String) request.getAttributes().get("id"));
		} catch (NumberFormatException e) {
			id = null;
		}

		this.id = id;

		this.protocol = (String) request.getAttributes().get("protocol");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#removeRepresentations()
	 */
	@Override
	public void removeRepresentations() throws ResourceException {
		if (id == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		{
			final String table = convertTable(TABLE_GROUPFILTERS);
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new QComparisonConstraint(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id));

			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		{
			final String table = convertTable(TABLE_GROUPFILTERGROUPS);
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new QComparisonConstraint(new CanonicalColumnName(table, "keyid"), QConstraint.CT_EQUALS,
					id));

			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		{
			final String table = convertTable(TABLE_GROUPFILTERRULES);
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new QComparisonConstraint(new CanonicalColumnName(table, "keyid"), QConstraint.CT_EQUALS,
					id));

			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}

	/*
	 * <root> <group> ... </group> </root>
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		if (id == null)
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

		final String key = getKey();
		if (key == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final String table = convertTable(TABLE_GROUPFILTERGROUPS);

		/*
		 * Groups that must be added
		 */
		final Set<Integer> newGroups = getGroupTagIDs(entity);
		
		final Map<Integer, Integer> existingGroups = new HashMap<Integer, Integer>();
		{
			final SelectQuery query = new SelectQuery();
			query.select(table, "groupid");
			query.select(table, "id");
			query.constrain(new CanonicalColumnName(table, "keyid"), QConstraint.CT_EQUALS, id);
			
			final Row.Set rs = new Row.Set();
			
			try {
				ec.doQuery(query, rs);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			for (Row row : rs.getSet())
				existingGroups.put(row.get("groupid").getInteger(), row.get("id").getInteger());
		}

		final Document ret = DocumentUtils.impl.newDocument();
		ret.appendChild(ret.createElement("root"));

		for (Integer groupID : newGroups) {
			if (existingGroups.containsKey(groupID)) {
				final Element entry = ret.createElement("group");
				entry.setAttribute("groupid", groupID.toString());
				entry.setAttribute("id", existingGroups.get(groupID).toString());
				entry.setAttribute("success", "true");
				entry.setTextContent("duplicate");
				ret.getDocumentElement().appendChild(entry);
				existingGroups.remove(groupID);
				continue;
			}
			else
				existingGroups.remove(groupID);
			
			final Row template;
			final Integer rowID;
			try {
				template = ec.getRow(table);
				template.get("id").setObject(rowID = Integer.valueOf((int) getRowID().get(ec, table, "id")));
				template.get("groupid").setObject(groupID);
				template.get("keyid").setObject(id);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}

			final InsertQuery query = new InsertQuery();
			query.setTable(table);
			query.setRow(template);

			final Element entry = ret.createElement("group");
			entry.setAttribute("groupid", groupID.toString());
			entry.setAttribute("id", rowID.toString());
			try {
				ec.doUpdate(query);
				entry.setAttribute("success", "true");
			} catch (DBException e) {
				entry.setAttribute("success", "false");
				entry.setTextContent(e.getMessage());
			}

			ret.getDocumentElement().appendChild(entry);
		}

		for (Integer rowID : existingGroups.values()) {
			final DeleteQuery query = new DeleteQuery();
			query.setTable(table);
			query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, rowID);
			
			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		
		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, ret));
	}

	private Set<Integer> getGroupTagIDs(Representation entity) throws ResourceException {
		final Set<Integer> newGroups = new HashSet<Integer>();
		final Document document = getDocument(entity);
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		for (Node node : nodes) {
			if (node.getNodeName().equals("group"))
				try {
					newGroups.add(Integer.valueOf(node.getTextContent()));
				} catch (NumberFormatException e) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				}
		}
		return newGroups;
	}

	private String getKey() throws ResourceException {
		final String filter = convertTable(TABLE_GROUPFILTERS);

		final SelectQuery query = new SelectQuery();
		query.select(filter, "key");
		query.constrain(new CanonicalColumnName(filter, "id"), QConstraint.CT_EQUALS, id);

		final Row.Loader rl = new Row.Loader();

		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}

		String key = null;
		if (rl.getRow() != null)
			key = rl.getRow().get("key").toString();

		return key;
	}

	public Representation represent(Variant variant) throws ResourceException {
		if (id == null)
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

		final String key = getKey();
		if (key == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

		Document document = null;

		if ("uri".equals(protocol))
			document = getGroupsByURI(new VFSPath(key));
		else if ("view".equals(protocol))
			document = getGroupsByView();

		if (document == null)
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Invalid protocol " + protocol);

		return new DomRepresentation(variant.getMediaType(), document);
	}

	private Document getGroupsByView() throws ResourceException {
		final String filterGroups = convertTable(TABLE_GROUPFILTERGROUPS);

		final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
		query.select(filterGroups, "groupid");
		query.select(convertTable(TABLE_GROUPS), "name");
		query.constrain(new CanonicalColumnName(filterGroups, "keyid"), QConstraint.CT_EQUALS, id);

		final Row.Set rs = new Row.Set();

		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			throw new ResourceException(e);
		}

		return QueryUtils.writeDocumentFromRowSet(rs.getSet());
	}

	private Document getGroupsByURI(VFSPath uri) throws ResourceException {
		final Collection<VFSPath> searchPaths = new ArrayList<VFSPath>();

		// Find the directory rules
		boolean cascade = false;
		{
			final String table = convertTable(TABLE_GROUPFILTERRULES);

			SelectQuery query = new SelectQuery();
			query.select(table, "rules");
			query.constrain(new CanonicalColumnName(table, "keyid"), QConstraint.CT_EQUALS, id);

			Row.Loader rl = new Row.Loader();
			try {
				ec.doQuery(query, rl);
				Row row = rl.getRow();
				Rules rules = new Rules();
				if (row != null) {
					rules.load(row.get("rules").toString());
					cascade = "true".equals(rules.rules.get("cascade"));
				}
			} catch (DBException e) {
				cascade = false;
			}

			if (cascade) {
				final VFSPath start = VFSPath.ROOT;
				searchPaths.add(start);
				for (VFSPathToken token : uri.getTokens())
					searchPaths.add(start.child(token));
			} else
				searchPaths.add(uri);
		}

		final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
		query.select(convertTable(TABLE_GROUPFILTERGROUPS), "groupid");
		query.select(convertTable(TABLE_GROUPS), "name");

		for (VFSPath path : searchPaths) {
			Integer id = findID(path);
			if (id != null)
				query.constrain(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName(
						convertTable(TABLE_GROUPFILTERGROUPS), "keyid"), QConstraint.CT_EQUALS, id));
		}

		final Row.Set rs = new Row.Set();

		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			throw new ResourceException(e);
		}

		return QueryUtils.writeDocumentFromRowSet(rs.getSet());
	}

	private Integer findID(VFSPath path) {
		final String table = convertTable(TABLE_GROUPFILTERS);

		final SelectQuery query = new SelectQuery();
		query.select(table, "id");
		query.constrain(new CanonicalColumnName(table, "key"), QConstraint.CT_EQUALS, path.toString());

		final Row.Loader rl = new Row.Loader();

		Integer id = null;
		try {
			ec.doQuery(query, rl);
			if (rl.getRow() != null)
				id = rl.getRow().get("id").getInteger();
		} catch (DBException e) {
			id = null;
		}

		return id;
	}

	private static class Rules {
		private HashMap<String, String> rules;

		public Rules() {
			rules = new HashMap<String, String>();
		}

		public void load(String data) {
			String[] split = data.split("&");
			for (String rule : split) {
				String[] current = rule.split("=");
				if (current.length == 2)
					rules.put(current[1], current[2]);
			}
		}
	}
}
