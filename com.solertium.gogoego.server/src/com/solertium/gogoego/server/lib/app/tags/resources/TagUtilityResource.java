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
package com.solertium.gogoego.server.lib.app.tags.resources;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.StaticRowID;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.gogoego.server.lib.app.tags.utils.TagV2ToTagV3;
import com.solertium.gogoego.server.lib.app.tags.utils.TagV3ToTagV4;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.utils.VFSUtils;

/**
 * TagUtilityResource.java
 * 
 * Handles more of the dirtier processes that the tag database may need or may
 * have needed. Should rarely be used and will likely be phased out soon.
 * 
 * @author carl.scott
 * 
 */
public class TagUtilityResource extends Resource {

	public static final String SEARCH_TABLE_VAR = "${GGE:TABLE}";
	public static final String SEARCH_COL_TAG = "TAG";
	public static final String SEARCH_COL_URI = "URI";

	private final ExecutionContext ec;
	private final VFS vfs;
	private final String siteID;
	private final String tableName;

	public TagUtilityResource(Context context, Request request, Response response) {
		super(context, request, response);
		TagApplication app = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION));
		ec = app.getExecutionContext();
		vfs = app.getVFS();
		siteID = app.getSiteID();
		tableName = siteID + "_tags";
		setModifiable(true);

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) {

		if (getRequest().getResourceRef().getPath().contains("evolve")) {

			return evolve();
		}

		else if (getRequest().getResourceRef().getPath().contains("clean")) {
			return clean();
		}

		else if (getRequest().getResourceRef().getPath().contains("upgrade")) {
			if ("3".equals(getRequest().getResourceRef().getQueryAsForm().getFirstValue("version")))
				return upgrade3();
			else if ("4".equals(getRequest().getResourceRef().getQueryAsForm().getFirstValue("version")))
				return upgrade4();
			else
				return null;
		}

		else
			return null;

	}

	private Representation upgrade3() {
		boolean testMode = !"false".equals(getRequest().getResourceRef().getQueryAsForm().getFirstValue("testmode"));
		try {
			new TagV2ToTagV3(siteID, vfs, ec, testMode) {
				public void initViewToTagList() {
					tagListToViewID.put("Regions", "resources");
					tagListToViewID.put("Materials", "resources");
					tagListToViewID.put("Topics", "resources");
					tagListToViewID.put("Partners", "expert");
					tagListToViewID.put("LegislationOrActivism", "legislation");
				}
			};
		} catch (DBException e) {
			return new StringRepresentation("Failed: " + e.getMessage());
		}
		return new StringRepresentation(testMode ? "Test run completed successfully."
				: "Upgrade completed successfully.");
	}

	private Representation upgrade4() {
		boolean testMode = !"false".equals(getRequest().getResourceRef().getQueryAsForm().getFirstValue("testmode"));

		final StringWriter out = new StringWriter();
		TagV3ToTagV4 upgrader = new TagV3ToTagV4(ec, siteID, testMode);
		upgrader.setWriter(out);
		try {
			upgrader.run();
		} catch (DBException e) {
			return new StringRepresentation("Failed: " + e.getMessage() + "<br/>" + out.toString());
		}

		return new StringRepresentation(testMode ? "Test run completed successfully.<br/>" + out.toString()
				: "Upgrade completed successfully\r\n" + out.toString());
	}

	private Representation clean() {
		final String uriTable = siteID + "_itemuris";
		final String tagTable = siteID + "_itemtags";

		SelectQuery sq = new SelectQuery();
		sq.select(uriTable, "*");

		Row.Set rs = new Row.Set();
		try {
			ec.doQuery(sq, rs);
		} catch (Exception e) {
			return null;
		}

		// Find any non-existant URIs, log their IDs, then delete them.
		HashMap<String, String> nonexistantURIs = new LinkedHashMap<String, String>();
		for (Row row : rs.getSet()) {
			try {
				if (!vfs.exists(VFSUtils.parseVFSPath(row.get("uri").toString()))) {
					String id = row.get("id").toString();

					nonexistantURIs.put(id, row.get("uri").toString());

					DeleteQuery query = new DeleteQuery();
					query.setTable(uriTable);
					query.constrain(new CanonicalColumnName(uriTable, "id"), QConstraint.CT_EQUALS, id);

					ec.doUpdate(query);
				}
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}

		final Document document = DocumentUtils.impl.newDocument();
		Element root = document.createElement("root");
		root.appendChild(DocumentUtils.impl.createCDATAElementWithText(document, "count", "Found "
				+ nonexistantURIs.size() + " non-existant URIs"));

		Iterator<Map.Entry<String, String>> iterator = nonexistantURIs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();

			DeleteQuery query = new DeleteQuery();
			query.setTable(tagTable);
			query.constrain(new CanonicalColumnName(tagTable, "uriid"), QConstraint.CT_EQUALS, entry.getKey());

			try {
				ec.doUpdate(query);
				root.appendChild(DocumentUtils.impl.createCDATAElementWithText(document, "success",
						"- Deleted tags for " + entry.getValue() + " (" + entry.getKey() + ")"));
			} catch (Exception e) {
				root.appendChild(DocumentUtils.impl.createCDATAElementWithText(document, "failure", "Could not delete "
						+ entry.getValue() + " (" + entry.getKey() + ")"));
			}
		}

		document.appendChild(root);
		return new DomRepresentation(MediaType.TEXT_XML, document);
	}

	private Representation evolve() {
		SelectQuery sq = new SelectQuery();
		sq.select(new CanonicalColumnName(tableName, "*"));

		Row.Set rs = new Row.Set();
		try {
			ec.doQuery(sq, rs);
		} catch (Exception e) {
			return null;
		}

		HashMap<String, ArrayList<String>> uriToTags = new HashMap<String, ArrayList<String>>();

		for (Row row : rs.getSet()) {
			String uri = row.get(SEARCH_COL_URI).toString();
			String tag = row.get(SEARCH_COL_TAG).toString();
			ArrayList<String> tags = uriToTags.get(uri);
			if (tags == null)
				tags = new ArrayList<String>();
			if (!tags.contains(tag))
				tags.add(tag);
			uriToTags.put(uri, tags);
		}

		Iterator<Map.Entry<String, ArrayList<String>>> iterator = uriToTags.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = iterator.next();

			// Add file entry
			String itemURITBL = siteID + "_itemuris";
			String itemURITBL_entryID = null;
			try {
				Row itemURIRow = ec.getRow(itemURITBL);
				itemURIRow.get("ID").setString(itemURITBL_entryID = "" + getRowID().get(ec, itemURITBL));
				itemURIRow.get("DATATYPE").setString("resource");
				itemURIRow.get("URI").setString(entry.getKey());

				InsertQuery addFile = new InsertQuery();
				addFile.setRow(itemURIRow);
				addFile.setTable(itemURITBL);

				ec.doUpdate(addFile);
			} catch (Exception e) {
				return null;
			}

			String itemTagsTBL = siteID + "_itemtags";
			for (String tag : entry.getValue()) {
				try {
					Row itemTagRow = ec.getRow(itemTagsTBL);
					itemTagRow.get("ID").setString(getRowID().get(ec, itemTagsTBL) + "");
					itemTagRow.get("URIID").setString(itemURITBL_entryID);
					itemTagRow.get("TAG").setString(tag);

					InsertQuery addTag = new InsertQuery();
					addTag.setRow(itemTagRow);
					addTag.setTable(itemTagsTBL);

					ec.doUpdate(addTag);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return new StringRepresentation("Success");
	}

	public void handlePost() {
		try {
			acceptRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	public void acceptRepresentation(Representation entity) throws ResourceException {
		String rep;
		try {
			rep = entity.getText();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}

		rep = doTemplating(rep);

		Document doc = DocumentUtils.impl.createDocumentFromString(rep);
		if (doc == null)
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);

		SelectQuery query = new SelectQuery();
		try {
			query.loadConfig(doc.getDocumentElement());
			GoGoDebug.get("debug").println("Query: " + query.getSQL(ec.getDBSession()));
		} catch (Exception e) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			getResponse().setEntity(
					new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(e.getMessage())));
		}

		Row.Set rs = new Row.Set();
		try {
			ec.doQuery(query, rs);
		} catch (Exception e) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			getResponse().setEntity(
					new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(e.getMessage())));
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(
				new DomRepresentation(MediaType.TEXT_XML, QueryUtils.writeDocumentFromRowSet(rs.getSet())));
	}

	public String doTemplating(final String sql) {
		if (sql == null)
			return sql;

		String template = sql;
		int index, tries = 0;

		while ((index = template.indexOf("$")) != -1 && tries < 25) {
			// final String context = template.substring(index + 2,
			// template.indexOf("}"));
			template = template.substring(0, index) + tableName + template.substring(template.indexOf("}") + 1);

			tries++;
		}

		return template;
	}

	public StaticRowID getRowID() {
		return StaticRowID.getInstance(new CanonicalColumnName(siteID + "_IDCOUNT", "id"), new CanonicalColumnName(
				siteID + "_IDCOUNT", "tbl"));
	}

}
