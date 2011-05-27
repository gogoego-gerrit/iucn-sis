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

import org.hibernate.Query;
import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.User;

public class UserDAO {
	
	public static User getUser(Session session, int id) throws PersistentException {
		User user = UserDAO.getUserByORMID(session, id);
		if (user != null && user.getState() == User.ACTIVE)
			return user;
		return null;
	}
	
	public static User[] getTrashedUsers(Session session) throws PersistentException {
		try {
			UserCriteria criteria = new UserCriteria(session);
			criteria.state.eq(User.DELETED);
			return listUserByCriteria(criteria);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	
	public static User getTrashedUser(Session session, int id) throws PersistentException {
		User assessment = UserDAO.getUserByORMID(session, id);
		if (assessment != null && assessment.getState() == User.DELETED)
			return assessment;
		return null;
	}
	
	public static User[] getUsersByCriteria(UserCriteria criteria) {
		criteria.state.eq(User.ACTIVE);
		return listUserByCriteria(criteria);		
	}
	
	public static User getUserByCriteria(UserCriteria criteria) {
		criteria.state.eq(User.ACTIVE);
		return loadUserByCriteria(criteria);
	}
	
	public static User[] getTrashedUsersByCriteria(UserCriteria criteria) {
		criteria.state.eq(User.DELETED);
		return listUserByCriteria(criteria);		
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	
	
	/*protected static User loadUserByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadUserByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	protected static User getUserByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getUserByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static User loadUserByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadUserByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static User getUserByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getUserByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static User loadUserByORMID(Session session, int id) throws PersistentException {
		try {
			return (User) session.load(User.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	protected static User getUserByORMID(Session session, int id) throws PersistentException {
		try {
			return (User) session.get(User.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	protected static User loadUserByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (User) session.load(User.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	protected static User getUserByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (User) session.get(User.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*protected static User[] listUserByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listUserByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	protected static User[] listUserByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listUserByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	protected static User[] listUserByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From User as User");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (User[]) list.toArray(new User[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	protected static User[] listUserByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From User as User");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (User[]) list.toArray(new User[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*protected static User loadUserByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadUserByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	protected static User loadUserByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadUserByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	protected static User loadUserByQuery(Session session, String condition, String orderBy) throws PersistentException {
		User[] users = listUserByQuery(session, condition, orderBy);
		if (users != null && users.length > 0)
			return users[0];
		else
			return null;
	}
	
	protected static User loadUserByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		User[] users = listUserByQuery(session, condition, orderBy, lockMode);
		if (users != null && users.length > 0)
			return users[0];
		else
			return null;
	}
	
	/*protected static java.util.Iterator iterateUserByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateUserByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	protected static java.util.Iterator iterateUserByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateUserByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	protected static java.util.Iterator iterateUserByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From User as User");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			return query.iterate();
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	protected static java.util.Iterator iterateUserByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From User as User");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			return query.iterate();
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static User createUser() {
		return new User();
	}
	
	/*public static boolean save(User user) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, user);
			return true;
		}
		catch (Exception e) {
			Debug.println("Error saving user:\n{0}", e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(User user) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, user);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	/**
	
	public static boolean deleteAndDissociate(User user)throws PersistentException {
		try {
			WorkingSet[] lWorking_sets = (WorkingSet[])user.getSubscribedWorkingSets().toArray(new WorkingSet[user.getSubscribedWorkingSets().size()]);
			for(int i = 0; i < lWorking_sets.length; i++) {
				lWorking_sets[i].getUser().remove(user);
			}
			Permission[] lPermissions = (Permission[])user.getPermission().toArray(new Permission[user.getPermission().size()]);
			for(int i = 0; i < lPermissions.length; i++) {
				lPermissions[i].getUser().remove(user);
			}
			WorkingSet[] lWorkingSets = (WorkingSet[])user.getWorkingSet().toArray(new WorkingSet[user.getWorkingSet().size()]);
			for(int i = 0; i < lWorkingSets.length; i++) {
				lWorkingSets[i].setCreator(null);
			}
			Edit[] lEdits = (Edit[])user.getEdit().toArray(new Edit[user.getEdit().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].setUser(null);
			}
			WorkflowNotes[] lWorkflowNotess = (WorkflowNotes[])user.getWorkflowNotes().toArray(new WorkflowNotes[user.getWorkflowNotes().size()]);
			for(int i = 0; i < lWorkflowNotess.length; i++) {
				lWorkflowNotess[i].setUser(null);
			}
			return delete(user);
		}
		catch(Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(User user, org.orm.Session session)throws PersistentException {
		try {
			WorkingSet[] lWorking_sets = (WorkingSet[])user.getSubscribedWorkingSets().toArray(new WorkingSet[user.getSubscribedWorkingSets().size()]);
			for(int i = 0; i < lWorking_sets.length; i++) {
				lWorking_sets[i].getUser().remove(user);
			}
			Permission[] lPermissions = (Permission[])user.getPermission().toArray(new Permission[user.getPermission().size()]);
			for(int i = 0; i < lPermissions.length; i++) {
				lPermissions[i].getUser().remove(user);
			}
			WorkingSet[] lWorkingSets = (WorkingSet[])user.getWorkingSet().toArray(new WorkingSet[user.getWorkingSet().size()]);
			for(int i = 0; i < lWorkingSets.length; i++) {
				lWorkingSets[i].setCreator(null);
			}
			Edit[] lEdits = (Edit[])user.getEdit().toArray(new Edit[user.getEdit().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].setUser(null);
			}
			WorkflowNotes[] lWorkflowNotess = (WorkflowNotes[])user.getWorkflowNotes().toArray(new WorkflowNotes[user.getWorkflowNotes().size()]);
			for(int i = 0; i < lWorkflowNotess.length; i++) {
				lWorkflowNotess[i].setUser(null);
			}
			try {
				session.delete(user);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		catch(Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	**/
	
	/*public static boolean refresh(User user) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(user);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(User user) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(user);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	protected static User loadUserByCriteria(UserCriteria userCriteria) {
		User[] users = listUserByCriteria(userCriteria);
		if(users == null || users.length == 0) {
			return null;
		}
		return users[0];
	}
	
	protected static User[] listUserByCriteria(UserCriteria userCriteria) {
		return userCriteria.listUser();
	}
}
