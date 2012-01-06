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
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMCriteria;
import org.iucn.sis.server.api.persistance.hibernate.BooleanExpression;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.User;

public class UserCriteria extends AbstractORMCriteria {
	public final IntegerExpression state;
	
	public final IntegerExpression id;
	public final StringExpression username;
	public final StringExpression firstName;
	public final StringExpression lastName;
	public final StringExpression initials;
	public final StringExpression affiliation;
	public final BooleanExpression sisUser;
	public final BooleanExpression rapidlistUser;
	public final StringExpression email;
	public final BooleanExpression offlineStatus;
	
	public UserCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		username = new StringExpression("username", this);
		firstName = new StringExpression("firstName", this);
		lastName = new StringExpression("lastName", this);
		initials = new StringExpression("initials", this);
		affiliation = new StringExpression("affiliation", this);
		sisUser = new BooleanExpression("sisUser", this);
		rapidlistUser = new BooleanExpression("rapidlistUser", this);
		email = new StringExpression("email", this);
		state = new IntegerExpression("state", this);
		offlineStatus = new BooleanExpression("offlineStatus", this);
	}
	
	public UserCriteria(Session session) {
		this(session.createCriteria(User.class));
	}
	
	public WorkingSetCriteria createWorking_setCriteria() {
		return new WorkingSetCriteria(createCriteria("working_set"));
	}
	
	public PermissionGroupCriteria createPermissionCriteria() {
		return new PermissionGroupCriteria(createCriteria("permission"));
	}
	
	public WorkingSetCriteria createWorkingSetCriteria() {
		return new WorkingSetCriteria(createCriteria("workingSet"));
	}
	
	public EditCriteria createEditCriteria() {
		return new EditCriteria(createCriteria("edit"));
	}

	
	public User uniqueUser() {
		return (User) super.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public User[] listUser() {
		java.util.List list = super.list();
		return (User[]) list.toArray(new User[list.size()]);
	}

	@Override
	public Criteria createAlias(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnlyInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Criteria setReadOnly(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}

