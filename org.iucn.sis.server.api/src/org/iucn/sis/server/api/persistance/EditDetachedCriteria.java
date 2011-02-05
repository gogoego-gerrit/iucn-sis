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
import org.iucn.sis.shared.api.models.Edit;

public class EditDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final DateExpression createdDate;
	
	public EditDetachedCriteria() throws ClassNotFoundException {
		super(Edit.class, EditCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		createdDate = new DateExpression("createdDate", this.getDetachedCriteria());
	}
	
	public EditDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, EditCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		createdDate = new DateExpression("createdDate", this.getDetachedCriteria());
	}
	
	public UserDetachedCriteria createUserCriteria() {
		return new UserDetachedCriteria(createCriteria("user"));
	}
	
	public WorkingSetDetachedCriteria createWorking_setCriteria() {
		return new WorkingSetDetachedCriteria(createCriteria("working_set"));
	}
	
	public AssessmentDetachedCriteria createAssessmentCriteria() {
		return new AssessmentDetachedCriteria(createCriteria("assessment"));
	}
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public NotesDetachedCriteria createNotesCriteria() {
		return new NotesDetachedCriteria(createCriteria("notes"));
	}
	
	public Edit uniqueEdit(Session session) {
		return (Edit) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Edit[] listEdit(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Edit[]) list.toArray(new Edit[list.size()]);
	}
}

