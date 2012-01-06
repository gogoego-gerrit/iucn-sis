package org.iucn.sis.server.api.io;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.ReferenceCriteria;
import org.iucn.sis.server.api.persistance.ReferenceDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.interfaces.HasReferences;

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
	
	public void addReference(HasReferences hasReferences, int referenceID) throws HibernateException {
		Reference reference = (Reference) session.get(Reference.class, referenceID);
		
		hasReferences.getReference().add(reference);
		session.update(hasReferences);
	}
	
	public boolean removeReference(HasReferences hasReferences, int referenceID) throws HibernateException {
		Reference reference = (Reference) session.get(Reference.class, referenceID);
		
		boolean found = hasReferences.getReference().remove(reference);
		if (found)
			session.update(hasReferences);
		
		return found;
	}

	public Reference[] getOfflineCreatedReferences() throws PersistentException {
		ReferenceCriteria criteria = new ReferenceCriteria(session);
		criteria.offlineStatus.eq(true);
		
		return ReferenceDAO.listReferenceByCriteria(criteria);
	}
}
