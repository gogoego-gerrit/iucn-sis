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
import org.iucn.sis.shared.api.models.CommonName;

public class CommonNameDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final IntegerExpression name;
	public final IntegerExpression principal;
	public final IntegerExpression validated;
	
	public CommonNameDetachedCriteria() throws ClassNotFoundException {
		super(CommonName.class, CommonNameCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new IntegerExpression("name", this.getDetachedCriteria());
		principal = new IntegerExpression("principal", this.getDetachedCriteria());
		validated = new IntegerExpression("validated", this.getDetachedCriteria());
	}
	
	public CommonNameDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, CommonNameCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new IntegerExpression("name", this.getDetachedCriteria());
		principal = new IntegerExpression("principal", this.getDetachedCriteria());
		validated = new IntegerExpression("validated", this.getDetachedCriteria());
	}
	
	public IsoLanguageDetachedCriteria createIso_languageCriteria() {
		return new IsoLanguageDetachedCriteria(createCriteria("iso_language"));
	}
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public ReferenceDetachedCriteria createReferenceCriteria() {
		return new ReferenceDetachedCriteria(createCriteria("reference"));
	}
	
	public NotesDetachedCriteria createNotesCriteria() {
		return new NotesDetachedCriteria(createCriteria("notes"));
	}
	
	public CommonName uniqueCommonName(Session session) {
		return (CommonName) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public CommonName[] listCommonName(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (CommonName[]) list.toArray(new CommonName[list.size()]);
	}
}

