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
import org.hibernate.classic.Session;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.PermissionResourceAttribute;

public class PermissionResourceAttributeDAO {
	public static PermissionResourceAttribute loadPermissionResourceAttributeByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPermissionResourceAttributeByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute getPermissionResourceAttributeByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getPermissionResourceAttributeByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPermissionResourceAttributeByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute getPermissionResourceAttributeByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getPermissionResourceAttributeByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByORMID(Session session, int id) throws PersistentException {
		try {
			return (PermissionResourceAttribute) session.load(PermissionResourceAttribute.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute getPermissionResourceAttributeByORMID(Session session, int id) throws PersistentException {
		try {
			return (PermissionResourceAttribute) session.get(PermissionResourceAttribute.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (PermissionResourceAttribute) session.load(PermissionResourceAttribute.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute getPermissionResourceAttributeByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (PermissionResourceAttribute) session.get(PermissionResourceAttribute.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute[] listPermissionResourceAttributeByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listPermissionResourceAttributeByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute[] listPermissionResourceAttributeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listPermissionResourceAttributeByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute[] listPermissionResourceAttributeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResourceAttribute as PermissionResourceAttribute");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (PermissionResourceAttribute[]) list.toArray(new PermissionResourceAttribute[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute[] listPermissionResourceAttributeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResourceAttribute as PermissionResourceAttribute");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (PermissionResourceAttribute[]) list.toArray(new PermissionResourceAttribute[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPermissionResourceAttributeByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPermissionResourceAttributeByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		PermissionResourceAttribute[] permissionResourceAttributes = listPermissionResourceAttributeByQuery(session, condition, orderBy);
		if (permissionResourceAttributes != null && permissionResourceAttributes.length > 0)
			return permissionResourceAttributes[0];
		else
			return null;
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		PermissionResourceAttribute[] permissionResourceAttributes = listPermissionResourceAttributeByQuery(session, condition, orderBy, lockMode);
		if (permissionResourceAttributes != null && permissionResourceAttributes.length > 0)
			return permissionResourceAttributes[0];
		else
			return null;
	}
	
	public static java.util.Iterator iteratePermissionResourceAttributeByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iteratePermissionResourceAttributeByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iteratePermissionResourceAttributeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iteratePermissionResourceAttributeByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iteratePermissionResourceAttributeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResourceAttribute as PermissionResourceAttribute");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			return query.iterate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iteratePermissionResourceAttributeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PermissionResourceAttribute as PermissionResourceAttribute");
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
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute createPermissionResourceAttribute() {
		return new PermissionResourceAttribute();
	}
	
	public static boolean save(PermissionResourceAttribute permissionResourceAttribute) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(permissionResourceAttribute);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(PermissionResourceAttribute permissionResourceAttribute) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(permissionResourceAttribute);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(PermissionResourceAttribute permissionResourceAttribute)throws PersistentException {
		try {
			if(permissionResourceAttribute.getPermission() != null) {
				permissionResourceAttribute.getPermission().getAttributes().remove(permissionResourceAttribute);
			}
			
			return delete(permissionResourceAttribute);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(PermissionResourceAttribute permissionResourceAttribute, Session session)throws PersistentException {
		try {
			if(permissionResourceAttribute.getPermission() != null) {
				permissionResourceAttribute.getPermission().getAttributes().remove(permissionResourceAttribute);
			}
			
			try {
				session.delete(permissionResourceAttribute);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean refresh(PermissionResourceAttribute permissionResourceAttribute) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(permissionResourceAttribute);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(PermissionResourceAttribute permissionResourceAttribute) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(permissionResourceAttribute);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PermissionResourceAttribute loadPermissionResourceAttributeByCriteria(PermissionResourceAttributeCriteria permissionResourceAttributeCriteria) {
		PermissionResourceAttribute[] permissionResourceAttributes = listPermissionResourceAttributeByCriteria(permissionResourceAttributeCriteria);
		if(permissionResourceAttributes == null || permissionResourceAttributes.length == 0) {
			return null;
		}
		return permissionResourceAttributes[0];
	}
	
	public static PermissionResourceAttribute[] listPermissionResourceAttributeByCriteria(PermissionResourceAttributeCriteria permissionResourceAttributeCriteria) {
		return permissionResourceAttributeCriteria.listPermissionResourceAttribute();
	}
}
