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
import org.iucn.sis.shared.api.models.PermissionGroup;

public class PermissionDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	
	public PermissionDetachedCriteria() throws ClassNotFoundException {
		super(PermissionGroup.class, PermissionGroupCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public PermissionDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, PermissionGroupCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
	}
	
	public PermissionDetachedCriteria createPermissionsCriteria() {
		return new PermissionDetachedCriteria(createCriteria("permissions"));
	}
	
	public UserDetachedCriteria createUserCriteria() {
		return new UserDetachedCriteria(createCriteria("user"));
	}
	
	public PermissionDetachedCriteria createParentPermissionCriteria() {
		return new PermissionDetachedCriteria(createCriteria("parentPermission"));
	}
	
	public PermissionResourceDetachedCriteria createPermissionResourceCriteria() {
		return new PermissionResourceDetachedCriteria(createCriteria("permissionResource"));
	}
	
	public PermissionGroup uniquePermission(Session session) {
		return (PermissionGroup) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public PermissionGroup[] listPermission(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (PermissionGroup[]) list.toArray(new PermissionGroup[list.size()]);
	}
}

