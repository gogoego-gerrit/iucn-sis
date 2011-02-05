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
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMCriteria;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.Field;

public class FieldCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	
	public FieldCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		name = new StringExpression("name", this);
	}
	
	public FieldCriteria(Session session) {
		this(session.createCriteria(Field.class));
	}
	
	public AssessmentCriteria createAssessmentCriteria() {
		return new AssessmentCriteria(createCriteria("assessment"));
	}
	
	public FieldCriteria createFieldsCriteria() {
		return new FieldCriteria(createCriteria("fields"));
	}
	
	public NotesCriteria createNotesCriteria() {
		return new NotesCriteria(createCriteria("notes"));
	}
	
	public FieldCriteria createParentCriteria() {
		return new FieldCriteria(createCriteria("parent"));
	}
	
	public ReferenceCriteria createReferenceCriteria() {
		return new ReferenceCriteria(createCriteria("reference"));
	}
	
	public PrimitiveFieldCriteria createPrimitiveFieldCriteria() {
		return new PrimitiveFieldCriteria(createCriteria("primitiveField"));
	}
	
	public Field uniqueField() {
		return (Field) super.uniqueResult();
	}
	
	public Field[] listField() {
		java.util.List list = super.list();
		return (Field[]) list.toArray(new Field[list.size()]);
	}

	@Override
	public Criteria createAlias(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnlyInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Criteria setReadOnly(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}

