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

import org.hibernate.classic.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMDetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.TaxonLevel;

public class TaxonLevelDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final IntegerExpression level;
	
	public TaxonLevelDetachedCriteria() throws ClassNotFoundException {
		super(TaxonLevel.class, TaxonLevelCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		level = new IntegerExpression("level", this.getDetachedCriteria());
	}
	
	public TaxonLevelDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, TaxonLevelCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		level = new IntegerExpression("level", this.getDetachedCriteria());
	}
	
	public TaxonDetachedCriteria createTaxaCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxa"));
	}
	
	public SynonymDetachedCriteria createSynonymCriteria() {
		return new SynonymDetachedCriteria(createCriteria("synonym"));
	}
	
	public TaxonLevel uniqueTaxonLevel(Session session) {
		return (TaxonLevel) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public TaxonLevel[] listTaxonLevel(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (TaxonLevel[]) list.toArray(new TaxonLevel[list.size()]);
	}
}

