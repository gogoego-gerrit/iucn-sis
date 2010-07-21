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
import org.iucn.sis.shared.api.models.Field;

public class FieldDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	
	public FieldDetachedCriteria() throws ClassNotFoundException{
		super(Field.class, FieldCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public FieldDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, FieldCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public AssessmentDetachedCriteria createAssessmentCriteria() {
		return new AssessmentDetachedCriteria(createCriteria("assessment"));
	}
	
	public FieldDetachedCriteria createFieldsCriteria() {
		return new FieldDetachedCriteria(createCriteria("fields"));
	}
	
	public NotesDetachedCriteria createNotesCriteria() {
		return new NotesDetachedCriteria(createCriteria("notes"));
	}
	
	public FieldDetachedCriteria createParentCriteria() {
		return new FieldDetachedCriteria(createCriteria("parent"));
	}
	
	public ReferenceDetachedCriteria createReferenceCriteria() {
		return new ReferenceDetachedCriteria(createCriteria("reference"));
	}
	
	public PrimitiveFieldDetachedCriteria createPrimitiveFieldCriteria() {
		return new PrimitiveFieldDetachedCriteria(createCriteria("primitiveField"));
	}
	
	public Field uniqueField(Session session) {
		return (Field) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Field[] listField(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Field[]) list.toArray(new Field[list.size()]);
	}
}

