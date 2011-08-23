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
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.User;

public class PermissionGroupDAO {
	
	/*
	public static PermissionGroup loadPermissionByORMID(int id) throws PersistentException {
	Session session = SISPersistentManager.instance().openSession();
	try {
		return loadPermissionByORMID(session, id);
	}
	catch (Exception e) {
		Debug.println(e);
		throw new PersistentException(e);
	} finally {
		session.close();
	}
	}
	
	public static PermissionGroup getPermissionByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getPermissionByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static PermissionGroup loadPermissionByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadPermissionByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static PermissionGroup getPermissionByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getPermissionByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	*/

	public static PermissionGroup loadPermissionByORMID(Session session, int id) throws PersistentException {
		try {
			return (PermissionGroup) session.load(PermissionGroup.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static PermissionGroup getPermissionByORMID(Session session, int id) throws PersistentException {
		try {
			return (PermissionGroup) session.get(PermissionGroup.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static PermissionGroup loadPermissionByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (PermissionGroup) session.load(PermissionGroup.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static PermissionGroup getPermissionByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (PermissionGroup) session.get(PermissionGroup.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	
	/*
	public static PermissionGroup[] listPermissionByQuery(String condition, String orderBy) throws PersistentException {
	Session session = SISPersistentManager.instance().openSession();
	try {
		return listPermissionByQuery(session, condition, orderBy);
	}
	catch (Exception e) {
		Debug.println(e);
		throw new PersistentException(e);
	} finally {
		session.close();
	}
	}
	
	public static PermissionGroup[] listPermissionByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listPermissionByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/	
	
	public static PermissionGroup[] listPermissionByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Permission as Permission");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (PermissionGroup[]) list.toArray(new PermissionGroup[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static PermissionGroup[] listPermissionByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Permission as Permission");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (PermissionGroup[]) list.toArray(new PermissionGroup[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	
	/*
	public static PermissionGroup loadPermissionByQuery(String condition, String orderBy) throws PersistentException {
	Session session = SISPersistentManager.instance().openSession();
	try {
		return loadPermissionByQuery(session, condition, orderBy);
	}
	catch (Exception e) {
		Debug.println(e);
		throw new PersistentException(e);
	} finally {
		session.close();
	}
	}
	
	public static PermissionGroup loadPermissionByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadPermissionByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/	
	
	public static PermissionGroup loadPermissionByQuery(Session session, String condition, String orderBy) throws PersistentException {
		PermissionGroup[] permissions = listPermissionByQuery(session, condition, orderBy);
		if (permissions != null && permissions.length > 0)
			return permissions[0];
		else
			return null;
	}
	
	public static PermissionGroup loadPermissionByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		PermissionGroup[] permissions = listPermissionByQuery(session, condition, orderBy, lockMode);
		if (permissions != null && permissions.length > 0)
			return permissions[0];
		else
			return null;
	}
	
	/*
	public static java.util.Iterator iteratePermissionByQuery(String condition, String orderBy) throws PersistentException {
	Session session = SISPersistentManager.instance().openSession();
	try {
		return iteratePermissionByQuery(session, condition, orderBy);
	}
	catch (Exception e) {
		Debug.println(e);
		throw new PersistentException(e);
	} finally {
		session.close();
	}
	}
	
	public static java.util.Iterator iteratePermissionByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iteratePermissionByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	
	public static java.util.Iterator iteratePermissionByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Permission as Permission");
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
	
	public static java.util.Iterator iteratePermissionByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Permission as Permission");
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
	
	public static PermissionGroup createPermission() {
		return new PermissionGroup();
	}
	
	/*
	public static boolean save(PermissionGroup permission) throws PersistentException {
	try {
		SISPersistentManager.instance().saveObject(session, permission);
		return true;
	}
	catch (Exception e) {
		Debug.println(e);
		throw new PersistentException(e);
	}
	}
	
	public static boolean delete(PermissionGroup permission) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, permission);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/	
	
	public static boolean deleteAndDissociate(PermissionGroup permission, Session session)throws PersistentException {
		try {
			if(permission.getParent() != null) {
				permission.getParent().getChildren().remove(permission);
			}
			User[] lUsers = (User[])permission.getUsers().toArray(new User[permission.getUsers().size()]);
			for(int i = 0; i < lUsers.length; i++) {
				lUsers[i].getPermissionGroups().remove(permission);
			}
			PermissionGroup[] lParentPermissions = (PermissionGroup[])permission.getChildren().toArray(new PermissionGroup[permission.getChildren().size()]);
			for(int i = 0; i < lParentPermissions.length; i++) {
				lParentPermissions[i].setPermissions(null);
			}
			Permission[] lPermissionResources = (Permission[])permission.getPermissions().toArray(new Permission[permission.getPermissions().size()]);
			for(int i = 0; i < lPermissionResources.length; i++) {
				session.delete(lPermissionResources[i]);
			}
			try {
				session.delete(permission);
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
	
	public static void savePermissionGroup(PermissionGroup permission, Session session)throws PersistentException {
		try {
			session.save(permission);
		} catch(Exception e) {
			Debug.println(e); 
			throw new PersistentException(e);
		}
	}	
	
	public static void updatePermissionGroup(PermissionGroup permission, Session session)throws PersistentException {
		try {
			session.update(permission);
		} catch(Exception e) {
			Debug.println(e); 
			throw new PersistentException(e);
		}
	}	
	
	/*
	public static boolean refresh(PermissionGroup permission) throws PersistentException {
	try {
		SISPersistentManager.instance().getSession().refresh(permission);
		return true;
	}
	catch (Exception e) {
		Debug.println(e);
		throw new PersistentException(e);
	}
	}
	
	public static boolean evict(PermissionGroup permission) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(permission);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	
	public static PermissionGroup loadPermissionByCriteria(PermissionGroupCriteria permissionCriteria) {
		PermissionGroup[] permissions = listPermissionByCriteria(permissionCriteria);
		if(permissions == null || permissions.length == 0) {
			return null;
		}
		return permissions[0];
	}
	
	public static PermissionGroup[] listPermissionByCriteria(PermissionGroupCriteria permissionCriteria) {
		return permissionCriteria.listPermission();
	}
}
