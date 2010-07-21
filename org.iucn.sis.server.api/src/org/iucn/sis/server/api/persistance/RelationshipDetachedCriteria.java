package org.iucn.sis.server.api.persistance;
/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import java.util.List;

import org.hibernate.classic.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMDetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.Relationship;

public class RelationshipDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	
	public RelationshipDetachedCriteria() throws ClassNotFoundException {
		super(Relationship.class, RelationshipCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public RelationshipDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, RelationshipCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public WorkingSetDetachedCriteria createWorkingSetCriteria() {
		return new WorkingSetDetachedCriteria(createCriteria("workingSet"));
	}
	
	public Relationship uniqueRelationship(Session session) {
		return (Relationship) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Relationship[] listRelationship(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Relationship[]) list.toArray(new Relationship[list.size()]);
	}
}

