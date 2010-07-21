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
import org.hibernate.classic.Session;
import org.hibernate.criterion.Criterion;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMCriteria;
import org.iucn.sis.server.api.persistance.hibernate.BooleanExpression;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.Taxon;

public class TaxonCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression friendlyName;
	public final BooleanExpression hybrid;
	public final StringExpression taxonomicAuthority;
	public final IntegerExpression state;
	
	public TaxonCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		name = new StringExpression("name", this);
		friendlyName = new StringExpression("friendlyName", this);
		hybrid = new BooleanExpression("hybrid", this);
		taxonomicAuthority = new StringExpression("taxonomicAuthority", this);
		state = new IntegerExpression("state",this);
	}
	
	public TaxonCriteria(Session session) {
		this(session.createCriteria(Taxon.class));
	}
	
	public TaxonCriteria() throws PersistentException {
		this(SISPersistentManager.instance().getSession());
	}
	
	public TaxonLevelCriteria createTaxonLevelCriteria() {
		return new TaxonLevelCriteria(createCriteria("taxonLevel"));
	}
	
	public TaxonStatusCriteria createTaxonStatusCriteria() {
		return new TaxonStatusCriteria(createCriteria("taxonStatus"));
	}
	
	public TaxonCriteria createTaxaCriteria() {
		return new TaxonCriteria(createCriteria("taxa"));
	}
	
	public WorkingSetCriteria createWorking_setCriteria() {
		return new WorkingSetCriteria(createCriteria("working_set"));
	}
	
	public ReferenceCriteria createReferenceCriteria() {
		return new ReferenceCriteria(createCriteria("reference"));
	}
	
	public TaxonCriteria createParentCriteria() {
		return new TaxonCriteria(createCriteria("parent"));
	}
	
	public EditCriteria createEditsCriteria() {
		return new EditCriteria(createCriteria("edits"));
	}
	
	public NotesCriteria createNotesCriteria() {
		return new NotesCriteria(createCriteria("notes"));
	}
	
	public AssessmentCriteria createAssessmentsCriteria() {
		return new AssessmentCriteria(createCriteria("assessments"));
	}
	
	public SynonymCriteria createSynonymsCriteria() {
		return new SynonymCriteria(createCriteria("synonyms"));
	}
	
	public CommonNameCriteria createCommonNamesCriteria() {
		return new CommonNameCriteria(createCriteria("CommonNames"));
	}
	
	public InfratypeCriteria createInfratypeCriteria() {
		return new InfratypeCriteria(createCriteria("infratype"));
	}
	
	public Taxon uniqueTaxon() {
		return (Taxon) super.uniqueResult();
	}
	
	public Taxon[] listTaxon() {
		java.util.List list = super.list();
		return (Taxon[]) list.toArray(new Taxon[list.size()]);
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

