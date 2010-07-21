package org.iucn.sis.server.api.io;

import org.iucn.sis.server.api.persistance.ReferenceCriteria;
import org.iucn.sis.server.api.persistance.ReferenceDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Reference;

public class ReferenceIO {
	
	public Reference getReferenceByHashCode(String hash) throws PersistentException {
		ReferenceCriteria criteria = new ReferenceCriteria();
		criteria.hash.eq(hash.toUpperCase());
		return ReferenceDAO.loadReferenceByCriteria(criteria);
	}

}
