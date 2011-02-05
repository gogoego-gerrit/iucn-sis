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
import org.iucn.sis.shared.api.models.TaxonStatus;

public class TaxonStatusDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression code;
	
	public TaxonStatusDetachedCriteria() throws ClassNotFoundException {
		super(TaxonStatus.class, TaxonStatusCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		code = new StringExpression("code", this.getDetachedCriteria());
	}
	
	public TaxonStatusDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, TaxonStatusCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		code = new StringExpression("code", this.getDetachedCriteria());
	}
	
	public TaxonDetachedCriteria createTaxaCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxa"));
	}
	
	public SynonymDetachedCriteria createSynonymCriteria() {
		return new SynonymDetachedCriteria(createCriteria("synonym"));
	}
	
	public TaxonStatus uniqueTaxonStatus(Session session) {
		return (TaxonStatus) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public TaxonStatus[] listTaxonStatus(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (TaxonStatus[]) list.toArray(new TaxonStatus[list.size()]);
	}
}

