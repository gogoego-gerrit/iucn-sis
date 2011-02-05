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

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMDetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.AssessmentType;

public class AssessmentTypeDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	
	public AssessmentTypeDetachedCriteria() throws ClassNotFoundException {
		super(AssessmentType.class, AssessmentTypeCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public AssessmentTypeDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, AssessmentTypeCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public WorkingSetDetachedCriteria createWorkingSetCriteria() {
		return new WorkingSetDetachedCriteria(createCriteria("workingSet"));
	}
	
	public AssessmentDetachedCriteria createAssessmentCriteria() {
		return new AssessmentDetachedCriteria(createCriteria("assessment"));
	}
	
	public AssessmentType uniqueAssessmentType(Session session) {
		return (AssessmentType) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public AssessmentType[] listAssessmentType(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (AssessmentType[]) list.toArray(new AssessmentType[list.size()]);
	}
}

