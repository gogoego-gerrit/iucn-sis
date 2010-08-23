/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.lib.app.tags.resources;

import org.gogoego.api.utils.DocumentUtils;
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

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.StaticRowID;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.gogoego.server.lib.app.tags.utils.TagDatabaseUtils;

/**
 * BaseTagResource.java
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class BaseTagResource extends Resource {

	public static final String TABLE_TAGS = "tags";
	public static final String TABLE_DEFAULTTAGS = "defaulttags";
	public static final String TABLE_GROUPS = "groups";
	public static final String TABLE_GROUPTAGS = "grouptags";
	public static final String TABLE_GROUPFILTERS = "groupkeys";
	public static final String TABLE_GROUPFILTERGROUPS = "groupkeygroups";
	public static final String TABLE_GROUPFILTERRULES = "groupkeyrules";
	public static final String TABLE_RESOURCEURIS = "resourceuris";
	public static final String TABLE_RESOURCETAGS = "resourcetags";

	protected final ExecutionContext ec;
	protected final String siteID;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public BaseTagResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		ec = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION))
				.getExecutionContext();

		siteID = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION)).getSiteID();

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	protected String convertTable(String table) {
		return convertTableBySite(table, siteID);
	}

	public static String convertTableBySite(String table, String siteID) {
		return siteID + "_" + table;
	}

	protected void getFailureDocument(Exception e) throws ResourceException {
		getFailureDocument(e, Status.SERVER_ERROR_INTERNAL);
	}

	protected void getFailureDocument(Exception e, Status status) throws ResourceException {
		GoGoDebug.get("debug").println("BaseTag Failure: {0}", e.getMessage());
		getResponse().setEntity(
				new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(e.getMessage())));
		throw new ResourceException(status, e);
	}

	protected Document getDocument(Representation entity) throws ResourceException {
		Document document = null;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			getFailureDocument(e, Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}
		return document;
	}

	protected StaticRowID getRowID() {
		return TagDatabaseUtils.getRowID(siteID);
	}

	protected Integer getTagID(final String tagName) throws DBException {
		final String table = convertTable(TABLE_TAGS);
		final SelectQuery query = new SelectQuery();
		query.select(table, "id");
		query.constrain(new CanonicalColumnName(table, "name"), QConstraint.CT_EQUALS, tagName);

		final Row.Loader rl = new Row.Loader();

		ec.doQuery(query, rl);

		return rl.getRow() != null ? rl.getRow().get("id").getInteger() : null;
	}

}
