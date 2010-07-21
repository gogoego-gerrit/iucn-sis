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
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.Infratype;

public class InfratypeDetachedCriteria extends AbstractORMDetachedCriteria {
	public final StringExpression code;
	public final StringExpression infraName;
	
	public InfratypeDetachedCriteria() throws ClassNotFoundException {
		super(Infratype.class, InfratypeCriteria.class);
		code = new StringExpression("code", this.getDetachedCriteria());
		infraName = new StringExpression("infraName", this.getDetachedCriteria());
	}
	
	public InfratypeDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, InfratypeCriteria.class);
		code = new StringExpression("code", this.getDetachedCriteria());
		infraName = new StringExpression("infraName", this.getDetachedCriteria());
	}
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public Infratype uniqueInfratype(Session session) {
		return (Infratype) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Infratype[] listInfratype(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Infratype[]) list.toArray(new Infratype[list.size()]);
	}
}

