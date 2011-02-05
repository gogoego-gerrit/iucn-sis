package org.iucn.sis.server.extensions.integrity;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.TransactionResource;
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

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.Query;
import com.solertium.db.utils.QueryUtils;

@SuppressWarnings("deprecation")
public class IntegrityDBResource extends TransactionResource {
	
	protected final ExecutionContext ec;
	
	private Status dbExceptionStatus;

	public IntegrityDBResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		ec = getExecutionContext();
		dbExceptionStatus = Status.SERVER_ERROR_INTERNAL;
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	protected ExecutionContext getExecutionContext() {
		return SIS.get().getLookupDatabase();
	}
	
	protected void doUpdate(Query query) throws ResourceException {
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new ResourceException(dbExceptionStatus, e);
		}
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
