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
import org.iucn.sis.shared.api.models.User;

public class UserDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression username;
	public final StringExpression firstName;
	public final StringExpression lastName;
	public final StringExpression initials;
	public final StringExpression affiliation;
	public final IntegerExpression sisUser;
	public final IntegerExpression rapidlistUser;
	public final StringExpression email;
	
	public UserDetachedCriteria() throws ClassNotFoundException {
		super(User.class, UserCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		username = new StringExpression("username", this.getDetachedCriteria());
		firstName = new StringExpression("firstName", this.getDetachedCriteria());
		lastName = new StringExpression("lastName", this.getDetachedCriteria());
		initials = new StringExpression("initials", this.getDetachedCriteria());
		affiliation = new StringExpression("affiliation", this.getDetachedCriteria());
		sisUser = new IntegerExpression("sisUser", this.getDetachedCriteria());
		rapidlistUser = new IntegerExpression("rapidlistUser", this.getDetachedCriteria());
		email = new StringExpression("email", this.getDetachedCriteria());
	}
	
	public UserDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, UserCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		username = new StringExpression("username", this.getDetachedCriteria());
		firstName = new StringExpression("firstName", this.getDetachedCriteria());
		lastName = new StringExpression("lastName", this.getDetachedCriteria());
		initials = new StringExpression("initials", this.getDetachedCriteria());
		affiliation = new StringExpression("affiliation", this.getDetachedCriteria());
		sisUser = new IntegerExpression("sisUser", this.getDetachedCriteria());
		rapidlistUser = new IntegerExpression("rapidlistUser", this.getDetachedCriteria());
		email = new StringExpression("email", this.getDetachedCriteria());
	}
	
	public WorkingSetDetachedCriteria createWorking_setCriteria() {
		return new WorkingSetDetachedCriteria(createCriteria("working_set"));
	}
	
	public PermissionDetachedCriteria createPermissionCriteria() {
		return new PermissionDetachedCriteria(createCriteria("permission"));
	}
	
	public WorkingSetDetachedCriteria createWorkingSetCriteria() {
		return new WorkingSetDetachedCriteria(createCriteria("workingSet"));
	}
	
	public EditDetachedCriteria createEditCriteria() {
		return new EditDetachedCriteria(createCriteria("edit"));
	}
	
	
	public User uniqueUser(Session session) {
		return (User) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public User[] listUser(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (User[]) list.toArray(new User[list.size()]);
	}
}

