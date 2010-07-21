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
import org.iucn.sis.shared.api.models.PrimitiveField;

public class PrimitiveFieldDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	
	public PrimitiveFieldDetachedCriteria() throws ClassNotFoundException {
		super(PrimitiveField.class, PrimitiveFieldCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public PrimitiveFieldDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, PrimitiveFieldCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public FieldDetachedCriteria createFieldCriteria() {
		return new FieldDetachedCriteria(createCriteria("field"));
	}
	
	public PrimitiveField uniquePrimitiveField(Session session) {
		return (PrimitiveField) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public PrimitiveField[] listPrimitiveField(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (PrimitiveField[]) list.toArray(new PrimitiveField[list.size()]);
	}
}

