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
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.AssessmentType;

public class AssessmentTypeCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	
	public AssessmentTypeCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		name = new StringExpression("name", this);
	}
	
	public AssessmentTypeCriteria(Session session) {
		this(session.createCriteria(AssessmentType.class));
	}
	
	public WorkingSetCriteria createWorkingSetCriteria() {
		return new WorkingSetCriteria(createCriteria("workingSet"));
	}
	
	public AssessmentCriteria createAssessmentCriteria() {
		return new AssessmentCriteria(createCriteria("assessment"));
	}
	
	public AssessmentType uniqueAssessmentType() {
		return (AssessmentType) super.uniqueResult();
	}
	
	public AssessmentType[] listAssessmentType() {
		java.util.List list = super.list();
		return (AssessmentType[]) list.toArray(new AssessmentType[list.size()]);
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
}

