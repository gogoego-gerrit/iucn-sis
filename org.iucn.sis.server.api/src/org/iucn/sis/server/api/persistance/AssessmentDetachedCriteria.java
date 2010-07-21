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
import org.iucn.sis.shared.api.models.Assessment;

public class AssessmentDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression source;
	public final StringExpression sourceDate;
	public final StringExpression internalId;
	
	public AssessmentDetachedCriteria() throws ClassNotFoundException {
		super(Assessment.class, AssessmentCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		source = new StringExpression("source", this.getDetachedCriteria());
		sourceDate = new StringExpression("sourceDate", this.getDetachedCriteria());
		internalId = new StringExpression("internalId", this.getDetachedCriteria());
	}
	
	public AssessmentDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, AssessmentCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		source = new StringExpression("source", this.getDetachedCriteria());
		sourceDate = new StringExpression("sourceDate", this.getDetachedCriteria());
		internalId = new StringExpression("internalId", this.getDetachedCriteria());
	}
	
	public AssessmentTypeDetachedCriteria createAssessment_typeCriteria() {
		return new AssessmentTypeDetachedCriteria(createCriteria("assessment_type"));
	}
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public EditDetachedCriteria createEditCriteria() {
		return new EditDetachedCriteria(createCriteria("edit"));
	}
	
	public ReferenceDetachedCriteria createReferenceCriteria() {
		return new ReferenceDetachedCriteria(createCriteria("reference"));
	}
	
	public FieldDetachedCriteria createFieldCriteria() {
		return new FieldDetachedCriteria(createCriteria("field"));
	}
	
	public Assessment uniqueAssessment(Session session) {
		return (Assessment) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Assessment[] listAssessment(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Assessment[]) list.toArray(new Assessment[list.size()]);
	}
}

