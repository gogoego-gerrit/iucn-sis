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
import org.iucn.sis.shared.api.models.IsoLanguage;

public class IsoLanguageDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression code;
	
	public IsoLanguageDetachedCriteria() throws ClassNotFoundException {
		super(IsoLanguage.class, IsoLanguageCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		code = new StringExpression("code", this.getDetachedCriteria());
	}
	
	public IsoLanguageDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, IsoLanguageCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		code = new StringExpression("code", this.getDetachedCriteria());
	}
	
	public CommonNameDetachedCriteria createCommonNameCriteria() {
		return new CommonNameDetachedCriteria(createCriteria("commonName"));
	}
	
	public IsoLanguage uniqueIsoLanguage(Session session) {
		return (IsoLanguage) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public IsoLanguage[] listIsoLanguage(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (IsoLanguage[]) list.toArray(new IsoLanguage[list.size()]);
	}
}

