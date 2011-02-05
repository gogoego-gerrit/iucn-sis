package org.iucn.sis.server.api.io;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.RelationshipCriteria;
import org.iucn.sis.server.api.persistance.RelationshipDAO;
import org.iucn.sis.shared.api.models.Relationship;

public class RelationshipIO {
	
	private final Session session;
	
	public RelationshipIO(Session session) {
		this.session = session;
	}

	public Relationship getRelationshipByName(String name) {
		RelationshipCriteria criteria = new RelationshipCriteria(session);
		
		criteria.name.eq(name);
		return RelationshipDAO.loadRelationshipByCriteria(criteria);
	}
	
}
