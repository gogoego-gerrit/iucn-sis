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
import org.iucn.sis.shared.api.models.Assessment;


public class AssessmentCriteria extends AbstractORMCriteria {
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	public final IntegerExpression state;

	public AssessmentCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		source = new StringExpression("source", this);
		sourceDate = new StringExpression("sourceDate", this);
		internalId = new StringExpression("internalId", this);
		state = new IntegerExpression("state", this);
	}
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	public final IntegerExpression id;
	public final StringExpression source;
	public final StringExpression sourceDate;
	public final StringExpression internalId;

	

	public AssessmentCriteria(Session session) {
		this(session.createCriteria(Assessment.class));
	}

	public AssessmentCriteria() throws PersistentException {
		this(SISPersistentManager.instance().getSession());
	}

	public AssessmentTypeCriteria createAssessment_typeCriteria() {
		return new AssessmentTypeCriteria(createCriteria("AssessmentType"));
	}

	public TaxonCriteria createTaxonCriteria() {
		return new TaxonCriteria(createCriteria("taxon"));
	}

	public EditCriteria createEditCriteria() {
		return new EditCriteria(createCriteria("edit"));
	}

	public ReferenceCriteria createReferenceCriteria() {
		return new ReferenceCriteria(createCriteria("reference"));
	}

	public FieldCriteria createFieldCriteria() {
		return new FieldCriteria(createCriteria("field"));
	}

	public Assessment uniqueAssessment() {
		return (Assessment) super.uniqueResult();
	}

	public Assessment[] listAssessment() {
		java.util.List list = super.list();
		return (Assessment[]) list.toArray(new Assessment[list.size()]);
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
