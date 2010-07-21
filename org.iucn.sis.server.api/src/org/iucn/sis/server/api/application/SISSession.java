package org.iucn.sis.server.api.application;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;

public class SISSession{
	
	protected Session session;
	protected SISTransaction transaction;
	
	
	public SISSession(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		
		return session;
	}
	
//	public Transaction getTransaction() {
//		
//	}
//	
//	public Transaction beginTransaction() {
//		
//	}
	
	
	
	
	

}
