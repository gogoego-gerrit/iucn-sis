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
import org.iucn.sis.shared.api.models.Notes;

public class NotesDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression value;
	
	public NotesDetachedCriteria() throws ClassNotFoundException {
		super(Notes.class, NotesCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		value = new StringExpression("value", this.getDetachedCriteria());
	}
	
	public NotesDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, NotesCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		value = new StringExpression("value", this.getDetachedCriteria());
	}
	
	public SynonymDetachedCriteria createSynonymCriteria() {
		return new SynonymDetachedCriteria(createCriteria("synonym"));
	}
	
	public CommonNameDetachedCriteria createCommon_nameCriteria() {
		return new CommonNameDetachedCriteria(createCriteria("common_name"));
	}
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public EditDetachedCriteria createEditCriteria() {
		return new EditDetachedCriteria(createCriteria("edit"));
	}
	
	public FieldDetachedCriteria createFieldCriteria() {
		return new FieldDetachedCriteria(createCriteria("field"));
	}
	
	public Notes uniqueNotes(Session session) {
		return (Notes) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Notes[] listNotes(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Notes[]) list.toArray(new Notes[list.size()]);
	}
}

