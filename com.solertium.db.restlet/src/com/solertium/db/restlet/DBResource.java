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

import org.restlet.Application;
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

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.Query;
import com.solertium.db.utils.QueryUtils;

public abstract class DBResource extends Resource {
	
	protected final ExecutionContext ec;
	
	private Status dbExceptionStatus;

	public DBResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		ec = getExecutionContext();
		dbExceptionStatus = Status.SERVER_ERROR_INTERNAL;
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	protected void doUpdate(Query query) throws ResourceException {
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(dbExceptionStatus, e);
		}
	}
	
	/**
	 * You may need to override this if you are not serving 
	 * your resource directly from your application or 
	 * your application does not implement DBProvidingApplication.
	 * @return the execution context
	 */
	protected ExecutionContext getExecutionContext() {
		final DBProvidingApplication dbpa;
		try {
			dbpa = ((DBProvidingApplication)Application.getCurrent());
		} catch (ClassCastException e) {
			throw new RuntimeException("Your application does not provide a database.");
		}
		
		return dbpa.getExecutionContext();
	}
	
	protected Row getRow(final Query query) throws ResourceException {
		final Row.Loader rl = new Row.Loader();
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(dbExceptionStatus, e);
		}
		return rl.getRow();
	}
	
	protected Row getRow(final String query) throws ResourceException {
		final Row.Loader rl = new Row.Loader();
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new ResourceException(dbExceptionStatus, e);
		}
		return rl.getRow();
	}
	
	protected Row.Set getRows(final Query query) throws ResourceException {
		final Row.Set rs = new Row.Set();
		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			throw new ResourceException(dbExceptionStatus, e);
		}
		return rs;
	}
	
	protected Row.Set getRows(final String query) throws ResourceException {
		final Row.Set rs = new Row.Set();
		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			throw new ResourceException(dbExceptionStatus, e);
		}
		return rs;
	}
	
	protected Document getRowsAsDocument(final Query query) throws ResourceException {
		return QueryUtils.writeDocumentFromRowSet(getRows(query).getSet());
	}
	
	protected Document getRowsAsDocument(final String query) throws ResourceException {
		return QueryUtils.writeDocumentFromRowSet(getRows(query).getSet());
	}
	
	protected Representation getRowsAsRepresentation(final Query query) throws ResourceException {
		return new DomRepresentation(MediaType.TEXT_XML, getRowsAsDocument(query));
	}
	
	protected Representation getRowsAsRepresentation(final String query) throws ResourceException {
		return new DomRepresentation(MediaType.TEXT_XML, getRowsAsDocument(query));
	}
	
	protected Number newID(String table) throws ResourceException {
		return newID(table, "id");
	}
	
	protected Number newID(String table, String checkColumn) throws ResourceException {
		try {
			return RowID.get(ec, table, checkColumn);
		} catch (DBException e) {
			throw new ResourceException(dbExceptionStatus, e);
		}
	}
	
	protected void setDBExceptionStatus(Status dbExceptionStatus) {
		this.dbExceptionStatus = dbExceptionStatus;
	}

}
