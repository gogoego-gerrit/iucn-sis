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

import com.solertium.db.CInteger;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.UpdateQuery;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;

public class WritableTableResource extends DBResource {
	
	private final String table;
	private final String id;

	public WritableTableResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		setModifiable(true);
		
		table = (String)request.getAttributes().get("table");
		id = (String)request.getAttributes().get("id");
	}
	
	/**
	 * This function will attempt to add any rows that you 
	 * specify into the specified table, and return the 
	 * newly created row ID in standard row format.
	 * 
	 * TODO: support multiple row insertion?
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		if (table == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "Please specify a table");
		
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
	
		final Row template;
		try {
			template = ec.getRow(table);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final Row row = new Row();
		
		final ElementCollection nodes = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("field")	
		);
		for (Element el : nodes) {
			final String fieldName = el.getAttribute("name");
			final Column column;
			if ((column = template.get(fieldName)) != null) {
				column.setString(el.getTextContent());
				row.add(column);
			}
		}
		
		final Integer id;
		try {
			id = Integer.valueOf((int)RowID.get(ec, table, "id"));
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create new ID for table \"" + table + "\".", e);
		}
		
		row.add(new CInteger("id", id));
		
		final InsertQuery query = new InsertQuery();
		query.setTable(table);
		query.setRow(row);
		
		doUpdate(query);

		getResponse().setStatus(Status.SUCCESS_CREATED);
		getResponse().setEntity(new StringRepresentation("<root><ID>" + id + "</ID></root>", MediaType.TEXT_XML));
	}
	
	/**
	 * Updates the column in the given {table} for the row 
	 * specified by the given row {id}
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (table == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "Please specify a table");
		
		final Integer id;
		try {
			id = Integer.parseInt(this.id);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a row ID");
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please speicyf a valid ID");
		}
		
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final Row template;
		try {
			template = ec.getRow(table);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final Row row = new Row();
		
		final ElementCollection nodes = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("field")	
		);
		
		for (Element el : nodes) {
			final String fieldName = el.getAttribute("name");
			final Column column;
			if ((column = template.get(fieldName)) != null) {
				column.setString(el.getTextContent());
				row.add(column);
			}
		}
		
		final UpdateQuery query = new UpdateQuery();
		query.setTable(table);
		query.setRow(row);
		query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);
		
		doUpdate(query);
		
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(createConfirmEntity("Row " + id + " updated in table " + table));
	}
	
	public void removeRepresentations() throws ResourceException {
		if (table == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "Please specify a table");

		final Integer id;
		try {
			id = Integer.parseInt(this.id);
		} catch (NullPointerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a row ID");
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please speicyf a valid ID");
		}
		
		final DeleteQuery query = new DeleteQuery();
		query.setTable(table);
		query.constrain(new CanonicalColumnName(table, "id"), QConstraint.CT_EQUALS, id);
		
		doUpdate(query);
		
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(createConfirmEntity("Row " + id + " deleted from table " + table));
	}

	private Representation createConfirmEntity(String message) {
		return new DomRepresentation(MediaType.TEXT_XML, 
			BaseDocumentUtils.impl.createConfirmDocument(message)
		);
	}
}
