package org.iucn.sis.server.api.restlets;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public abstract class TransactionRestlet extends Restlet {
	
	public TransactionRestlet(Context context) {
		super(context);
	}
	
	/**
	 * Handles a call in a single transaction.  If you have a
	 * call that is going to do multiple transactions, close 
	 * the session's initial transaction before starting anew.
	 */
	public final void handle(Request request, Response response) {
		final boolean openTransation = shouldOpenTransation(request, response);
		final Session session = SISPersistentManager.instance().openSession();
		if (openTransation)
			session.beginTransaction();
		
		boolean success;
		try {
			handle(request, response, session);
			success = response.getStatus().isSuccess();
		} catch (Throwable e) {
			Debug.println("(!) {0} threw uncaught exception:\r\n{1}", getClass().getSimpleName(), e);
			success = false;
		}
		
		try {
			if (openTransation) {
				if (success)
					session.getTransaction().commit();
				else
					session.getTransaction().rollback();
			}
		} catch (HibernateException e) {
			Debug.println("Hibernate Error: {0}\n{1}", e.getMessage(), e);
			if (e.getCause() instanceof BatchUpdateException) {
				BatchUpdateException cause = (BatchUpdateException)e.getCause();
				SQLException sql = cause.getNextException();
				Debug.println("Caused by SQL Exception: {0}\n{1}", sql.getMessage(), sql);
			}
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Hibernate Exception Occurred");
			if (openTransation)
				session.getTransaction().rollback();
		} finally {
			session.close();
		}
	}
	
	protected boolean shouldOpenTransation(Request request, Response response) {
		return !Method.GET.equals(request.getMethod());
	}
	
	/**
	 * A safe handle method, given a new session.  Please handle your 
	 * own exceptions.
	 * @param request
	 * @param response
	 * @param session
	 */
	protected abstract void handle(Request request, Response response, Session session);

}
