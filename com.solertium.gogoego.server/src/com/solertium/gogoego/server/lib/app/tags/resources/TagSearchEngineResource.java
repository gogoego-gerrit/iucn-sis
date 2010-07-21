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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
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
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.CookieUtility;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.util.restlet.ScratchResource;

/**
 * TagSearchEngineResource.java
 * 
 * Provides the ability to perform complex searches on the tag database.
 * 
 * @author carl.scott
 * 
 */
public class TagSearchEngineResource extends BaseTagResource {

	public static final String SEARCH_STORAGE_URI_BASE = "/apps/tags/storedresults/";

	private final int mode;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public TagSearchEngineResource(Context context, Request request, Response response) {
		super(context, request, response);
		String mode = request.getResourceRef().getQueryAsForm().getFirstValue("mode");
		this.mode = (mode != null && !mode.equals("") && mode.equals("like")) ? QConstraint.CT_CONTAINS
				: QConstraint.CT_EQUALS;

		setModifiable(true);
	}

	public void handlePost() {
		try {
			acceptRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	public Representation represent(Variant variant) {
		try {
			Form form = getRequest().getResourceRef().getQueryAsForm();
			doSearch(form, false);
		} catch (ResourceException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		return getResponse().getEntity();
	}

	public void acceptRepresentation(Representation entity) throws ResourceException {
		try {
			doSearch(new Form(entity), true);
		} catch (ResourceException f) {
			f.printStackTrace();
			throw new ResourceException(f.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}
	}

	public void doSearch(Form form, boolean decoded) throws ResourceException {
		final ArrayList<String> ids = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			public boolean addAll(java.util.Collection<? extends String> c) {
				for (String k : c)
					if (!contains(c))
						add(k);
				return true;
			};
		};

		final ArrayList<String> uris = new ArrayList<String>(), tags = new ArrayList<String>();

		fillData(form, uris, tags, decoded);

		for (String uri : uris)
			ids.addAll(getUriIDs(uri));

		final String redirect = form.getFirstValue("redirect");

		/*
		 * If user was actually looking for IDs but had invalid entry...
		 */
		if (ids.isEmpty() && !uris.isEmpty()) {
			noResults(redirect);
			return;
		}

		final String resourceTagTable = convertTable(TABLE_RESOURCETAGS);
		final String resourceUriTable = convertTable(TABLE_RESOURCEURIS);

		ExperimentalSelectQuery query = new ExperimentalSelectQuery();
		query.select(resourceTagTable, "*");
		query.select(convertTable(TABLE_TAGS), "name as tag");
		query.select(resourceUriTable, "uri");

		QConstraintGroup curi = new QConstraintGroup();
		for (String id : ids)
			curi.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName(resourceTagTable,
					"uriid"), QConstraint.CT_EQUALS, id));
		if (!ids.isEmpty())
			query.constrain(curi);

		QConstraintGroup ctag = new QConstraintGroup();
		for (String tag : tags) {
			// TODO: optimize
			Integer tagID;
			try {
				tagID = getTagID(tag);
			} catch (DBException e) {
				tagID = null;
			}
			if (tagID == null)
				continue;
			ctag.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName(resourceTagTable,
					"tagid"), QConstraint.CT_EQUALS, tagID));
		}

		if (!tags.isEmpty())
			query.constrain(ctag);

		try {
			//GoGoDebug.get("debug").println(query.getSQL(ec.getDBSession()));

			/*
			 * CS First do AND Query...
			 */
			/*
			 * final Row.Set andResults = performANDSearch(ids, tags,
			 * app.getExecutionContext()); final ArrayList<SearchResultRow>
			 * resultRows = new ArrayList<SearchResultRow>(); for (Row row :
			 * andResults.getSet()) resultRows.add(new SearchResultRow(row));
			 */
			/*
			 * Now do OR query
			 */
			Row.Set orResults = new Row.Set();
			ec.doQuery(query, orResults);

			/*
			 * for (Row row : orResults.getSet()) resultRows.add(new
			 * SearchResultRow(row));
			 */

			// Document doc = QueryUtils.writeDocumentFromRowSet(resultRows);
			Document doc = QueryUtils.writeDocumentFromRowSet(orResults.getSet());
			Element queryElement = doc.createElement("query");
			for (String uri : uris)
				queryElement.appendChild(DocumentUtils.impl.createElementWithText(doc, "uri", uri));
			for (String tag : tags)
				queryElement.appendChild(DocumentUtils.impl.createElementWithText(doc, "tag", tag));
			doc.getDocumentElement().appendChild(queryElement);

			/**
			 * If the user wants to redirect, generate a key, save the results,
			 * and do the redirect.
			 */

			if (redirect != null) {
				final Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 1);

				final String key = CookieUtility.newUniqueID();

				ServerApplication.getFromContext(getContext()).getScratchResourceBin().add(
						new ScratchResource(new Reference(SEARCH_STORAGE_URI_BASE + key), key, cal.getTime(), doc));
				
				Request req = getRequest();
				if (req instanceof InternalRequest)
					req = ((InternalRequest)req).getFirstRequest();
				
				final String redirection = (req.getReferrerRef() != null ? 
						req.getReferrerRef().getHostIdentifier() : 
						req.getResourceRef().getHostIdentifier()) + 
						redirect + (redirect.indexOf("?") == -1 ? "?" : "&") + 
						"key=" + key;

				getResponse().redirectSeeOther(redirection);
				getResponse().setEntity(new GoGoEgoStringRepresentation(
					"See <a href=\"" + redirection + "\">" + 
					redirection + "</a>", MediaType.TEXT_HTML
				));
			} else {
				String template = form.getFirstValue("template");
				if (template != null && !template.equals("")) {
					TemplateRegistry reg = ServerApplication.getFromContext(getContext()).getTemplateRegistry();
					if (reg.isRegistered(template)) {
						Element meta = doc.createElement("meta");
						meta.setAttribute("content", template);
						meta.setAttribute("name", "template");
						doc.getDocumentElement().appendChild(meta);
					}
				}

				getResponse().setEntity(new SearchResultsRepresentation(MediaType.TEXT_XML, doc));
				getResponse().getAttributes().put(Constants.FORCE_MAGIC, Boolean.TRUE);
			}
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
	}

	/*
	 * private static class SearchResultRow extends Row {
	 * 
	 * public SearchResultRow(Row row) { super(row); }
	 * 
	 * public boolean equals(Object obj) { if (!(obj instanceof
	 * SearchResultRow)) return false; String myID = getID(); if (myID == null)
	 * return ((SearchResultRow)obj).getID() == null; return
	 * getID().equals(((SearchResultRow)obj).getID()); }
	 * 
	 * public String getID() { try { return get("id").toString(); } catch
	 * (NullPointerException e) { return null; } } }
	 */

	/*
	 * private Row.Set performANDSearch(ArrayList<String> uriIDs,
	 * ArrayList<String> tags, ExecutionContext ec) throws DBException { final
	 * String tagTable = app.getSiteID() + "_itemtags"; final String uriTable =
	 * app.getSiteID() + "_itemuris";
	 * 
	 * final String tagTableFields = tagTable + ".id, " + tagTable + ".tag, " +
	 * tagTable + ".uriID"; final String uriTableFields = uriTable + ".uri, " +
	 * uriTable + ".datatype";
	 * 
	 * final String sql; if (tags.size() < 1) { SelectQuery query = new
	 * SelectQuery(); query.select(tagTable, "*"); query.select(uriTable, "*");
	 * if (!tags.isEmpty()) query.constrain( new CanonicalColumnName(tagTable,
	 * "tag"), QConstraint.CT_EQUALS, tags.get(0) ); QConstraintGroup uris = new
	 * QConstraintGroup(); for (int i = 0; i < uriIDs.size(); i++)
	 * uris.addConstraint(QConstraint.CG_AND, new QComparisonConstraint( new
	 * CanonicalColumnName(uriTable, "uriID"), QConstraint.CT_EQUALS,
	 * uriIDs.get(i)) ); query.constrain(uris);
	 * 
	 * sql = query.getSQL(ec.getDBSession()); } else { final StringBuilder
	 * builder = new StringBuilder(); builder.append("SELECT Q1.* FROM (" +
	 * "(SELECT " + tagTableFields + ", " + uriTableFields + " FROM " + tagTable
	 * + ", " + uriTable + " WHERE " + tagTable + ".tag = '" + tags.get(0) +
	 * "') AS Q1" ); for (int i = 1; i < tags.size(); i++) {
	 * builder.append(" JOIN " + "(SELECT " + tagTableFields + ", " +
	 * uriTableFields + " FROM " + tagTable + ", " + uriTable + " WHERE " +
	 * tagTable + ".tag = '" + tags.get(i) + "') AS Q" + (i+1) + " ON Q" + i +
	 * ".uriid = " + "Q" + (i+1) + ".uriid" ); }
	 * 
	 * builder.append(")");
	 * 
	 * if (!uriIDs.isEmpty()) { builder.append(" AND ("); for (int i = 0; i <
	 * uriIDs.size(); i++) builder.append("Q1." + uriTable + ".uriID = '" +
	 * uriIDs.get(i) + "'" + (i+1 < uriIDs.size() ? " OR " : "") ); }
	 * 
	 * sql = builder.toString(); }
	 * 
	 * System.out.println("-------------------"); System.out.println(sql);
	 * 
	 * Row.Set rs = new Row.Set();
	 * 
	 * ec.doQuery(sql, rs);
	 * 
	 * 
	 * System.out.println("-------------------"); System.out.println("Query
	 * Executed, found " + rs.getSet().size() + " AND results.");
	 * System.out.println("-------------------");
	 * 
	 * return rs; }
	 */

	private void fillData(Form form, ArrayList<String> uris, ArrayList<String> tags, boolean decoded) {
		String uriList = form.getValues("uri");
		if (uriList != null && !uriList.equals("")) {
			String[] csv = uriList.split(",");
			for (String uri : csv)
				try {
					uris.add(decoded ? uri : URLDecoder.decode(uri, "UTF-8"));
				} catch (Exception e) {
					uris.add(uri);
				}
		}

		String tagList = form.getValues("tag");
		if (tagList != null && !tagList.equals("")) {
			String[] csv = tagList.split(",");
			for (String tag : csv)
				if (tag != null && !tag.equals("") && !tag.equals("null"))
					try {
						tags.add(decoded ? tag : URLDecoder.decode(tag, "UTF-8"));
					} catch (Exception e) {
						tags.add(tag);
					}
		}
	}

	private ArrayList<String> getUriIDs(String uri) {
		final String table = convertTable(TABLE_RESOURCEURIS);
		SelectQuery idQuery = new SelectQuery();
		idQuery.select(table, "id");
		idQuery.constrain(new CanonicalColumnName(table, "uri"), mode, uri);

		final ArrayList<String> ids = new ArrayList<String>();
		try {
			//System.out.println(idQuery.getSQL(ec.getDBSession()));
			Row.Set rs = new Row.Set();
			ec.doQuery(idQuery, rs);

			for (Row row : rs.getSet())
				ids.add(row.get("id").toString());

		} catch (DBException e) {
			TrivialExceptionHandler.ignore(this, e);
		} catch (NullPointerException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		return ids;
	}

	private void noResults(String redirect) {
		final Document doc = DocumentUtils.impl.createDocumentFromString("<result></result>");
		if (redirect != null) {
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);

			final String key = CookieUtility.newUniqueID();

			ServerApplication.getFromContext(getContext()).getScratchResourceBin().add(
					new ScratchResource(new Reference(SEARCH_STORAGE_URI_BASE + key), key, cal.getTime(), doc));

			getResponse().redirectSeeOther(redirect + (redirect.indexOf("?") == -1 ? "?" : "&") + "key=" + key);
			getResponse().setEntity(
					new StringRepresentation("See <a href=\"" + redirect + "\">" + redirect + "</a>",
							MediaType.TEXT_HTML));
		} else {
			getResponse().setStatus(Status.SUCCESS_OK);
			getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
		}
	}
}
