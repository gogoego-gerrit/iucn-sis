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
import org.iucn.sis.shared.api.models.Notes;

public class NotesCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression value;
	
	public NotesCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		value = new StringExpression("value", this);
	}
	
	public NotesCriteria(Session session) {
		this(session.createCriteria(Notes.class));
	}
	
	public SynonymCriteria createSynonymCriteria() {
		return new SynonymCriteria(createCriteria("synonym"));
	}
	
	public CommonNameCriteria createCommon_nameCriteria() {
		return new CommonNameCriteria(createCriteria("common_name"));
	}
	
	public TaxonCriteria createTaxonCriteria() {
		return new TaxonCriteria(createCriteria("taxon"));
	}
	
	public EditCriteria createEditCriteria() {
		return new EditCriteria(createCriteria("edit"));
	}
	
	public FieldCriteria createFieldCriteria() {
		return new FieldCriteria(createCriteria("field"));
	}
	
	public Notes uniqueNotes() {
		return (Notes) super.uniqueResult();
	}
	
	public Notes[] listNotes() {
		java.util.List list = super.list();
		return (Notes[]) list.toArray(new Notes[list.size()]);
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

