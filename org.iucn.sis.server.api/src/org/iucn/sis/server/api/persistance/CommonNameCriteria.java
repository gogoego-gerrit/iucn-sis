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
import org.iucn.sis.shared.api.models.CommonName;

public class CommonNameCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final IntegerExpression principal;
	public final IntegerExpression validated;
	
	public CommonNameCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		name = new StringExpression("name", this);
		principal = new IntegerExpression("principal", this);
		validated = new IntegerExpression("validated", this);
	}
	
	public CommonNameCriteria(Session session) {
		this(session.createCriteria(CommonName.class));
	}
	
	public IsoLanguageCriteria createIso_languageCriteria() {
		return new IsoLanguageCriteria(createCriteria("iso_language"));
	}
	
	public TaxonCriteria createTaxonCriteria() {
		return new TaxonCriteria(createCriteria("taxon"));
	}
	
	public ReferenceCriteria createReferenceCriteria() {
		return new ReferenceCriteria(createCriteria("reference"));
	}
	
	public NotesCriteria createNotesCriteria() {
		return new NotesCriteria(createCriteria("notes"));
	}
	
	public CommonName uniqueCommonName() {
		return (CommonName) super.uniqueResult();
	}
	
	public CommonName[] listCommonName() {
		java.util.List list = super.list();
		return (CommonName[]) list.toArray(new CommonName[list.size()]);
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

