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
import org.iucn.sis.shared.api.models.Permission;

public class PermissionResourceDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression url;
	
	public PermissionResourceDetachedCriteria() throws ClassNotFoundException {
		super(Permission.class, PermissionResourceCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		url = new StringExpression("url", this.getDetachedCriteria());
	}
	
	public PermissionResourceDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, PermissionResourceCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		url = new StringExpression("url", this.getDetachedCriteria());
	}
	
	public PermissionDetachedCriteria createPermissionCriteria() {
		return new PermissionDetachedCriteria(createCriteria("permission"));
	}
	
	
	
	public PermissionResourceAttributeDetachedCriteria createPermissionResourceAttributeCriteria() {
		return new PermissionResourceAttributeDetachedCriteria(createCriteria("permissionResourceAttribute"));
	}
	
	public Permission uniquePermissionResource(Session session) {
		return (Permission) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Permission[] listPermissionResource(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Permission[]) list.toArray(new Permission[list.size()]);
	}
}

