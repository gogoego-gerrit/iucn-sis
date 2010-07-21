package org.iucn.sis.server.api.io;

import org.iucn.sis.server.api.persistance.RelationshipCriteria;
import org.iucn.sis.server.api.persistance.RelationshipDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Relationship;

public class RelationshipIO {

	public Relationship getRelationshipByName(String name) {
		RelationshipCriteria criteria;
		try {
			criteria = new RelationshipCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		criteria.name.eq(name);
		return RelationshipDAO.loadRelationshipByCriteria(criteria);
	}
	
}
