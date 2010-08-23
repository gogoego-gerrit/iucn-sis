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
package com.solertium.db.restlet;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.Query;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.BaseDocumentUtils;

public class QueryResource extends DBResource {
	
	public QueryResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		return new StringRepresentation(
			"<html><head><title>Quick Query</title></head><body><h2>Enter Query XML</h2>" +
			"<form method=\"POST\" action=\"" + getRequest().getResourceRef() + "\">" +
			"<textarea name=\"xml\" rows=\"40\" cols=\"120\"></textarea><br/>" +
			"<input type=\"submit\" value=\"Submit\" /></body></html>", 
			MediaType.TEXT_HTML
		);
	}
	
	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final String text;
		try {
			text = entity.getText();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		Document doc = BaseDocumentUtils.impl.createDocumentFromString(text);
		if (doc == null)
			parseForm(text);
		else {
			try {
				process(doc);
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}
	
	private void parseForm(String text) throws ResourceException {
		Form form;
		try {
			form = new Form(text);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		try {
			process(BaseDocumentUtils.impl.createDocumentFromString(form.getFirstValue("xml")));
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	private void process(Document document) throws ResourceException {
		Query query;
		final String queryType = getRequest().getResourceRef().getQueryAsForm().getFirstValue("queryType", "simple");
		if ("experimental".equals(queryType)) {
			query = newExperimentalSelectQuery();
			try {
				((ExperimentalSelectQuery)query).loadConfig(document.getDocumentElement());
			} catch (Exception e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
			}
		}
		else {
			query = new SelectQuery();
			try {
				((SelectQuery)query).loadConfig(document.getDocumentElement());
			} catch (Exception e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
			}
		}
		
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(getRowsAsRepresentation(query));
	}
	
	protected Query newExperimentalSelectQuery() {
		return new ExperimentalSelectQuery();
	}

}
