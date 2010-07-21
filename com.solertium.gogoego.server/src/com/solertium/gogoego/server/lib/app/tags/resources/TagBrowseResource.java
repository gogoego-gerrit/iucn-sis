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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QConstraintGroup;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;
import com.solertium.util.restlet.CookieUtility;
import com.solertium.util.restlet.ScratchResource;

/**
 * TagBrowseResource.java
 * 
 * A simple means of browsing for information based on a given uri or set of
 * tags. There are two protocols:
 * 
 * 1) uri: provide a URI in your request, and the response will contain all tags
 * that are associated with the given URI in the database
 * 
 * 2) tag: provide a comma-separated list of tags and the response will contain
 * all URIs that match at least one of those tags.
 * 
 * It is meant to be a simple and straightforward means of accessing data and is
 * not to be complex or complicated. To do more complicated lookups, the
 * TagSearchEngineResource should be used.
 * 
 * @author carl.scott
 * 
 */
public class TagBrowseResource extends BaseTagResource {

	public static final String SEARCH_KEY = "$SEARCH_KEY";

	private final String remaining;
	private final String mode;

	private final String template;

	private final TemplateRegistry templateRegistry;

	public TagBrowseResource(Context context, Request request, Response response) {
		super(context, request, response);
		int index;
		String rem = request.getResourceRef().getRemainingPart();
		remaining = ((index = rem.indexOf("?")) != -1) ? rem.substring(0, index) : rem;

		mode = request.getAttributes().get("mode").equals("uri") ? "uri" : "tag";

		String possibleTemplate = request.getResourceRef().getQueryAsForm().getFirstValue("template");
		template = (possibleTemplate == null || possibleTemplate.equals("")) ? null : possibleTemplate;

		templateRegistry = ServerApplication.getFromContext(context).getTemplateRegistry();
	}

	public Representation represent(Variant variant) throws ResourceException {
		final String resourceTagTable = convertTable(TABLE_RESOURCETAGS);
		final String resourceUriTable = convertTable(TABLE_RESOURCEURIS);

		if (mode.equals("uri")) {
			final Form form = getRequest().getResourceRef().getQueryAsForm();

			String mode = form.getFirstValue("mode");
			int constraint = QConstraint.CT_EQUALS;
			if (mode != null && !mode.equals("") && mode.equals("like")) {
				constraint = QConstraint.CT_CONTAINS;
			}

			SelectQuery idQuery = new SelectQuery();
			idQuery.select(resourceUriTable, "id");
			idQuery.constrain(new CanonicalColumnName(resourceUriTable, "uri"), constraint, remaining);

			final ArrayList<String> ids = new ArrayList<String>();
			try {
				Row.Set rs = new Row.Set();
				ec.doQuery(idQuery, rs);

				for (Row row : rs.getSet())
					ids.add(row.get("id").toString());
			} catch (DBException e) {
				return null;
			} catch (NullPointerException e) {
				return new GoGoEgoDomRepresentation(variant.getMediaType(), getEmptyResults());
			}

			if (ids.isEmpty())
				return new GoGoEgoDomRepresentation(variant.getMediaType(), getEmptyResults());

			ExperimentalSelectQuery query = new ExperimentalSelectQuery();
			query.select(resourceTagTable, "*");
			query.select(convertTable(TABLE_TAGS), "name as tag");
			query.select(resourceUriTable, "uri");

			QConstraintGroup uris = new QConstraintGroup();
			for (String id : ids)
				uris.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName(
						resourceTagTable, "uriid"), QConstraint.CT_EQUALS, id));
			query.constrain(uris);

			final ArrayList<String> queriedTags = new ArrayList<String>();
			Iterator<String> it = form.getNames().iterator();

			QConstraintGroup cg = new QConstraintGroup();
			boolean addTagConstraint = false;
			while (it.hasNext()) {
				String cur = it.next();
				if (cur.equals("tag")) {
					String tags = getRequest().getResourceRef().getQueryAsForm().getValues(cur);
					if (tags != null && !tags.equals("")) {
						addTagConstraint = true;

						QConstraintGroup group = new QConstraintGroup();

						String[] tagList = tags.split(",");
						for (String tag : tagList) {
							// TODO: optimize
							Integer tagID;
							try {
								tagID = getTagID(tag);
							} catch (DBException e) {
								tagID = null;
							}
							if (tagID == null)
								continue;

							queriedTags.add(tag);
							group.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName(
									resourceTagTable, "tagid"), QConstraint.CT_EQUALS, tagID));
						}

						cg.addConstraint(QConstraintGroup.CG_AND, group);
					}
				}
			}
			if (addTagConstraint)
				query.constrain(cg);

			try {
				//GoGoDebug.get("debug").println(query.getSQL(ec.getDBSession()));
				Row.Set rs = new Row.Set();
				ec.doQuery(query, rs);

				Document doc = QueryUtils.writeDocumentFromRowSet(rs.getSet());

				Element queryElement = doc.createElement("query");
				queryElement.appendChild(DocumentUtils.impl.createElementWithText(doc, "uri", remaining));
				for (String tag : queriedTags)
					queryElement.appendChild(DocumentUtils.impl.createElementWithText(doc, "tag", tag));
				doc.getDocumentElement().appendChild(queryElement);

				if (template != null) {
					if (template.equals(SEARCH_KEY)) {
						final Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, 1);

						final String key = CookieUtility.newUniqueID();

						ServerApplication.getFromContext(getContext()).getScratchResourceBin().add(
								new ScratchResource(
										new Reference(TagSearchEngineResource.SEARCH_STORAGE_URI_BASE + key), key, cal
												.getTime(), doc));

						return new GoGoEgoStringRepresentation(key, MediaType.TEXT_PLAIN);
					}

					if (templateRegistry.isRegistered(template)) {
						Element meta = doc.createElement("meta");
						meta.setAttribute("content", template);
						meta.setAttribute("name", "template");
						doc.getDocumentElement().appendChild(meta);
					}
				}

				getResponse().getAttributes().put(Constants.FORCE_MAGIC, Boolean.TRUE);
								
				return new SearchResultsRepresentation(variant.getMediaType(), doc);
			} catch (Exception e) {
				return null;
			}
		} else {
			ExperimentalSelectQuery query = new ExperimentalSelectQuery();
			query.select(resourceUriTable, "*");
			query.select(convertTable(TABLE_TAGS), "name");
			String[] tagList = remaining.replaceAll("/", "").split(",");
			if (remaining.replaceAll("/", "").length() > 0) {
				for (String tag : tagList) {
					// TODO: optimize
					Integer tagID;
					try {
						tagID = getTagID(tag);
					} catch (DBException e) {
						tagID = null;
					}
					if (tagID == null)
						continue;

					query.constrain(QConstraint.CG_OR, new CanonicalColumnName(resourceTagTable, "tagid"),
							QConstraint.CT_EQUALS, tagID);
				}
			}

			try {
				Row.Set rs = new Row.Set();
				ec.doQuery(query, rs);

				Document doc = QueryUtils.writeDocumentFromRowSet(rs.getSet());
				if (template != null) {
					if (templateRegistry.isRegistered(template)) {
						Element meta = doc.createElement("meta");
						meta.setAttribute("content", template);
						meta.setAttribute("name", "template");
						doc.getDocumentElement().appendChild(meta);
					}
				}

				getResponse().getAttributes().put(Constants.FORCE_MAGIC, Boolean.TRUE);
				
				return new SearchResultsRepresentation(variant.getMediaType(), doc);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private Document getEmptyResults() {
		Document doc = DocumentUtils.impl.createDocumentFromString("<result></result>");
		if (template != null) {
			if (templateRegistry.isRegistered(template)) {
				Element meta = doc.createElement("meta");
				meta.setAttribute("content", template);
				meta.setAttribute("name", "template");
				doc.getDocumentElement().appendChild(meta);
			}
		}
		return doc;
	}

}
