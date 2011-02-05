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
import org.iucn.sis.server.api.persistance.hibernate.DateExpression;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.shared.api.models.Edit;

public class EditCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final DateExpression createdDate;
	
	
	public EditCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		createdDate = new DateExpression("createdDate", this);
	}
	
	public EditCriteria(Session session) {
		this(session.createCriteria(Edit.class));
	}
	
	public UserCriteria createUserCriteria() {
		return new UserCriteria(createCriteria("user"));
	}
	
	public WorkingSetCriteria createWorking_setCriteria() {
		return new WorkingSetCriteria(createCriteria("working_set"));
	}
	
	public AssessmentCriteria createAssessmentCriteria() {
		return new AssessmentCriteria(createCriteria("assessment"));
	}
	
	public TaxonCriteria createTaxonCriteria() {
		return new TaxonCriteria(createCriteria("taxon"));
	}
	
	public NotesCriteria createNotesCriteria() {
		return new NotesCriteria(createCriteria("notes"));
	}
	
	public Edit uniqueEdit() {
		return (Edit) super.uniqueResult();
	}
	
	public Edit[] listEdit() {
		java.util.List list = super.list();
		return (Edit[]) list.toArray(new Edit[list.size()]);
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

