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
import org.iucn.sis.shared.api.models.Synonym;

public class SynonymCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression friendlyName;
	public final StringExpression genusName;
	public final StringExpression speciesName;
	public final StringExpression infraType;
	public final StringExpression infraName;
	public final StringExpression stockName;
	public final StringExpression genusAuthor;
	public final StringExpression speciesAuthor;
	public final StringExpression infrarankAuthor;
	
	public SynonymCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		friendlyName = new StringExpression("friendlyName", this);
		genusName = new StringExpression("genusName", this);
		speciesName = new StringExpression("speciesName", this);
		infraType = new StringExpression("infraType", this);
		infraName = new StringExpression("infraName", this);
		stockName = new StringExpression("stockName", this);
		genusAuthor = new StringExpression("genusAuthor", this);
		speciesAuthor = new StringExpression("speciesAuthor", this);
		infrarankAuthor = new StringExpression("infrarankAuthor", this);
	}
	
	public SynonymCriteria(Session session) {
		this(session.createCriteria(Synonym.class));
	}
	
	public TaxonStatusCriteria createTaxon_statusCriteria() {
		return new TaxonStatusCriteria(createCriteria("taxon_status"));
	}
	
	public TaxonLevelCriteria createTaxon_levelCriteria() {
		return new TaxonLevelCriteria(createCriteria("taxon_level"));
	}
	
	public TaxonCriteria createTaxonCriteria() {
		return new TaxonCriteria(createCriteria("taxon"));
	}
	
	public NotesCriteria createNotesCriteria() {
		return new NotesCriteria(createCriteria("notes"));
	}
	
	public ReferenceCriteria createReferenceCriteria() {
		return new ReferenceCriteria(createCriteria("reference"));
	}
	
	public Synonym uniqueSynonym() {
		return (Synonym) super.uniqueResult();
	}
	
	public Synonym[] listSynonym() {
		java.util.List list = super.list();
		return (Synonym[]) list.toArray(new Synonym[list.size()]);
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

