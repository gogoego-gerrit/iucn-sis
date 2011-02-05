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
import org.iucn.sis.server.api.persistance.hibernate.DateExpression;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.WorkingSet;

public class WorkingSetDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression description;
	public final DateExpression createdDate;
	
	public WorkingSetDetachedCriteria() throws ClassNotFoundException {
		super(WorkingSet.class, WorkingSetCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		description = new StringExpression("description", this.getDetachedCriteria());
		createdDate = new DateExpression("createdDate", this.getDetachedCriteria());
	}
	
	public WorkingSetDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, WorkingSetCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		description = new StringExpression("description", this.getDetachedCriteria());
		createdDate = new DateExpression("createdDate", this.getDetachedCriteria());
	}
	
	public RelationshipDetachedCriteria createRelationshipCriteria() {
		return new RelationshipDetachedCriteria(createCriteria("relationship"));
	}
	
	public AssessmentTypeDetachedCriteria createAssessment_typeCriteria() {
		return new AssessmentTypeDetachedCriteria(createCriteria("assessment_type"));
	}
	
	public UserDetachedCriteria createUser1Criteria() {
		return new UserDetachedCriteria(createCriteria("user1"));
	}
	
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public RegionDetachedCriteria createRegionCriteria() {
		return new RegionDetachedCriteria(createCriteria("region"));
	}
	
	public EditDetachedCriteria createEditCriteria() {
		return new EditDetachedCriteria(createCriteria("edit"));
	}
	
	public UserDetachedCriteria createUserCriteria() {
		return new UserDetachedCriteria(createCriteria("user"));
	}
	
	public WorkingSet uniqueWorkingSet(Session session) {
		return (WorkingSet) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public WorkingSet[] listWorkingSet(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (WorkingSet[]) list.toArray(new WorkingSet[list.size()]);
	}
}

