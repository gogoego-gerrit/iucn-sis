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
import org.iucn.sis.shared.api.models.Permission;

public class PermissionResourceDAO {
	/*public static Permission loadPermissionResourceByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadPermissionResourceByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Permission getPermissionResourceByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getPermissionResourceByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Permission loadPermissionResourceByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadPermissionResourceByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Permission getPermissionResourceByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getPermissionResourceByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Permission loadPermissionResourceByORMID(Session session, int id) throws PersistentException {
		try {
			return (Permission) session.load(Permission.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Permission getPermissionResourceByORMID(Session session, int id) throws PersistentException {
		try {
			return (Permission) session.get(Permission.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Permission loadPermissionResourceByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Permission) session.load(Permission.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Permission getPermissionResourceByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Permission) session.get(Permission.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Permission[] listPermissionResourceByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listPermissionResourceByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Permission[] listPermissionResourceByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listPermissionResourceByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Permission[] listPermissionResourceByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResource as PermissionResource");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Permission[]) list.toArray(new Permission[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Permission[] listPermissionResourceByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResource as PermissionResource");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Permission[]) list.toArray(new Permission[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Permission loadPermissionResourceByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadPermissionResourceByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Permission loadPermissionResourceByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadPermissionResourceByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Permission loadPermissionResourceByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Permission[] permissionResources = listPermissionResourceByQuery(session, condition, orderBy);
		if (permissionResources != null && permissionResources.length > 0)
			return permissionResources[0];
		else
			return null;
	}
	
	public static Permission loadPermissionResourceByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Permission[] permissionResources = listPermissionResourceByQuery(session, condition, orderBy, lockMode);
		if (permissionResources != null && permissionResources.length > 0)
			return permissionResources[0];
		else
			return null;
	}
	
	/*public static java.util.Iterator iteratePermissionResourceByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iteratePermissionResourceByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static java.util.Iterator iteratePermissionResourceByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iteratePermissionResourceByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static java.util.Iterator iteratePermissionResourceByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResource as PermissionResource");
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
	
	public static java.util.Iterator iteratePermissionResourceByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResource as PermissionResource");
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
	
	public static Permission createPermissionResource() {
		return new Permission();
	}
	
	/*public static boolean save(Permission permissionResource) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, permissionResource);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Permission permissionResource) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, permissionResource);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Permission permissionResource)throws PersistentException {
		try {
			if(permissionResource.getPermissionGroup() != null) {
				permissionResource.getPermissionGroup().getChildren().remove(permissionResource);
			}
			
			
			return delete(permissionResource);
		}
		catch(Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static boolean deleteAndDissociate(Permission permissionResource, Session session)throws PersistentException {
		try {
			if(permissionResource.getPermissionGroup() != null) {
				permissionResource.getPermissionGroup().getChildren().remove(permissionResource);
			}
			
			try {
				session.delete(permissionResource);
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
	
	/*public static boolean refresh(Permission permissionResource) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(permissionResource);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Permission permissionResource) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(permissionResource);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static Permission loadPermissionResourceByCriteria(PermissionResourceCriteria permissionResourceCriteria) {
		Permission[] permissionResources = listPermissionResourceByCriteria(permissionResourceCriteria);
		if(permissionResources == null || permissionResources.length == 0) {
			return null;
		}
		return permissionResources[0];
	}
	
	public static Permission[] listPermissionResourceByCriteria(PermissionResourceCriteria permissionResourceCriteria) {
		return permissionResourceCriteria.listPermissionResource();
	}
}
