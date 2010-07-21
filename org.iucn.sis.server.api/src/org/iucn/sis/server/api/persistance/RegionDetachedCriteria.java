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
import org.iucn.sis.shared.api.models.Region;

public class RegionDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression description;
	
	public RegionDetachedCriteria() throws ClassNotFoundException {
		super(Region.class, RegionCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		description = new StringExpression("description", this.getDetachedCriteria());
	}
	
	public RegionDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, RegionCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		description = new StringExpression("description", this.getDetachedCriteria());
	}
	
	public WorkingSetDetachedCriteria createWorking_setCriteria() {
		return new WorkingSetDetachedCriteria(createCriteria("working_set"));
	}
	
	public Region uniqueRegion(Session session) {
		return (Region) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Region[] listRegion(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Region[]) list.toArray(new Region[list.size()]);
	}
}

