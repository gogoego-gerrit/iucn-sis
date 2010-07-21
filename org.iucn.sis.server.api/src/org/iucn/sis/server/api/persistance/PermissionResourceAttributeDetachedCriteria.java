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
import org.iucn.sis.shared.api.models.PermissionResourceAttribute;

public class PermissionResourceAttributeDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression regex;
	
	public PermissionResourceAttributeDetachedCriteria() throws ClassNotFoundException {
		super(PermissionResourceAttribute.class, PermissionResourceAttributeCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		regex = new StringExpression("regex", this.getDetachedCriteria());
	}
	
	public PermissionResourceAttributeDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, PermissionResourceAttributeCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		regex = new StringExpression("regex", this.getDetachedCriteria());
	}
	
	public PermissionResourceDetachedCriteria createPermission_resourceCriteria() {
		return new PermissionResourceDetachedCriteria(createCriteria("permission_resource"));
	}
	
	public PermissionResourceAttribute uniquePermissionResourceAttribute(Session session) {
		return (PermissionResourceAttribute) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public PermissionResourceAttribute[] listPermissionResourceAttribute(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (PermissionResourceAttribute[]) list.toArray(new PermissionResourceAttribute[list.size()]);
	}
}

