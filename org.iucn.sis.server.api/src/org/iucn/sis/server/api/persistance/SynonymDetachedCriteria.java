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
import org.iucn.sis.shared.api.models.Synonym;

public class SynonymDetachedCriteria extends AbstractORMDetachedCriteria {
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
	
	public SynonymDetachedCriteria() throws ClassNotFoundException {
		super(Synonym.class, SynonymCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		friendlyName = new StringExpression("friendlyName", this.getDetachedCriteria());
		genusName = new StringExpression("genusName", this.getDetachedCriteria());
		speciesName = new StringExpression("speciesName", this.getDetachedCriteria());
		infraType = new StringExpression("infraType", this.getDetachedCriteria());
		infraName = new StringExpression("infraName", this.getDetachedCriteria());
		stockName = new StringExpression("stockName", this.getDetachedCriteria());
		genusAuthor = new StringExpression("genusAuthor", this.getDetachedCriteria());
		speciesAuthor = new StringExpression("speciesAuthor", this.getDetachedCriteria());
		infrarankAuthor = new StringExpression("infrarankAuthor", this.getDetachedCriteria());
	}
	
	public SynonymDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, SynonymCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		friendlyName = new StringExpression("friendlyName", this.getDetachedCriteria());
		genusName = new StringExpression("genusName", this.getDetachedCriteria());
		speciesName = new StringExpression("speciesName", this.getDetachedCriteria());
		infraType = new StringExpression("infraType", this.getDetachedCriteria());
		infraName = new StringExpression("infraName", this.getDetachedCriteria());
		stockName = new StringExpression("stockName", this.getDetachedCriteria());
		genusAuthor = new StringExpression("genusAuthor", this.getDetachedCriteria());
		speciesAuthor = new StringExpression("speciesAuthor", this.getDetachedCriteria());
		infrarankAuthor = new StringExpression("infrarankAuthor", this.getDetachedCriteria());
	}
	
	public TaxonStatusDetachedCriteria createTaxon_statusCriteria() {
		return new TaxonStatusDetachedCriteria(createCriteria("taxon_status"));
	}
	
	public TaxonLevelDetachedCriteria createTaxon_levelCriteria() {
		return new TaxonLevelDetachedCriteria(createCriteria("taxon_level"));
	}
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public NotesDetachedCriteria createNotesCriteria() {
		return new NotesDetachedCriteria(createCriteria("notes"));
	}
	
	public ReferenceDetachedCriteria createReferenceCriteria() {
		return new ReferenceDetachedCriteria(createCriteria("reference"));
	}
	
	public Synonym uniqueSynonym(Session session) {
		return (Synonym) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Synonym[] listSynonym(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Synonym[]) list.toArray(new Synonym[list.size()]);
	}
}

