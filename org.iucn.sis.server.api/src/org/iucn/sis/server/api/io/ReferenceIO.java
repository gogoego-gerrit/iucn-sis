package org.iucn.sis.server.api.io;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.ReferenceCriteria;
import org.iucn.sis.server.api.persistance.ReferenceDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Reference;

public class ReferenceIO {
	
	private final Session session;
	
	public ReferenceIO(Session session) {
		this.session = session;
	}
	
	public Reference getReferenceByHashCode(String hash) throws PersistentException {
		ReferenceCriteria criteria = new ReferenceCriteria(session);
		criteria.hash.eq(hash.toUpperCase());
		return ReferenceDAO.loadReferenceByCriteria(criteria);
	}

}
