package org.iucn.sis.server.api.restlets;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

public abstract class TransactionResource extends Resource {
	
	public TransactionResource(Context context, Request request, Response response) {
		super(context, request, response);
	}
	
	@Override
	public final Representation represent(Variant variant) throws ResourceException {
		final Session session = openSession();
		
		Representation representation = null;
		
		boolean success;
		try {
			representation = represent(variant, session);
			success = getResponse().getStatus().isSuccess();
		} catch (Throwable e) {
			Debug.println("(!) {0} threw uncaught exception:\r\n{1}", getClass().getSimpleName(), e);
			success = false;
		}
		
		closeSession(session, success);
		
		return representation;
	}
	
	public Representation represent(Variant variant, Session session) throws ResourceException {
		if (isReadable())
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	public final void acceptRepresentation(Representation entity) throws ResourceException {
		final Session session = openSession();
		
		boolean success;
		try {
			acceptRepresentation(entity, session);
			success = getResponse().getStatus().isSuccess();
		} catch (Throwable e) {
			Debug.println("(!) {0} threw uncaught exception:\r\n{1}", getClass().getSimpleName(), e);
			success = false;
		}
		
		closeSession(session, success);
	}
	
	public void acceptRepresentation(Representation entity, Session session) throws ResourceException {
		if (isModifiable())
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	@Override
	public final void storeRepresentation(Representation entity) throws ResourceException {
		final Session session = openSession();
		
		boolean success;
		try {
			storeRepresentation(entity, session);
			success = getResponse().getStatus().isSuccess();
		} catch (Throwable e) {
			Debug.println("(!) {0} threw uncaught exception:\r\n{1}", getClass().getSimpleName(), e);
			success = false;
		}
		
		closeSession(session, success);
	}
	
	public void storeRepresentation(Representation entity, Session session) throws ResourceException {
		if (isModifiable())
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	@Override
	public final void removeRepresentations() throws ResourceException {
		final Session session = openSession();
		
		boolean success;
		try {
			removeRepresentations(session);
			success = getResponse().getStatus().isSuccess();
		} catch (Throwable e) {
			Debug.println("(!) {0} threw uncaught exception:\r\n{1}", getClass().getSimpleName(), e);
			success = false;
		}
		
		closeSession(session, success);
	}
	
	public void removeRepresentations(Session session) throws ResourceException {
		if (isModifiable())
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * Open a new session and start a transaction.
	 * @return
	 */
	private Session openSession() {
		Session session = SISPersistentManager.instance().openSession();
		session.beginTransaction();
		
		return session;
	}
	
	private void closeSession(Session session, boolean success) {
		try {
			if (success)
				session.getTransaction().commit();
			else
				session.getTransaction().rollback();
		} catch (HibernateException e) {
			Debug.println("Hibernate Error: {0}\n{1}", e.getMessage(), e);
			if (e.getCause() instanceof BatchUpdateException) {
				BatchUpdateException cause = (BatchUpdateException)e.getCause();
				SQLException sql = cause.getNextException();
				Debug.println("Caused by SQL Exception: {0}\n{1}", sql.getMessage(), sql);
			}
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "Hibernate Exception Occurred");
			session.getTransaction().rollback();
		} finally {
			session.close();
		}
	}

}
