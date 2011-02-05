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
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.WorkingSet;

public class RelationshipDAO {
	/*public static Relationship loadRelationshipByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadRelationshipByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Relationship getRelationshipByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getRelationshipByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Relationship loadRelationshipByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadRelationshipByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Relationship getRelationshipByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getRelationshipByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Relationship loadRelationshipByORMID(Session session, int id) throws PersistentException {
		try {
			return (Relationship) session.load(Relationship.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Relationship getRelationshipByORMID(Session session, int id) throws PersistentException {
		try {
			return (Relationship) session.get(Relationship.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Relationship loadRelationshipByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Relationship) session.load(Relationship.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Relationship getRelationshipByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Relationship) session.get(Relationship.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Relationship[] listRelationshipByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listRelationshipByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Relationship[] listRelationshipByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listRelationshipByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Relationship[] listRelationshipByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Relationship as Relationship");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Relationship[]) list.toArray(new Relationship[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Relationship[] listRelationshipByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Relationship as Relationship");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Relationship[]) list.toArray(new Relationship[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Relationship loadRelationshipByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadRelationshipByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Relationship loadRelationshipByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadRelationshipByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Relationship loadRelationshipByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Relationship[] relationships = listRelationshipByQuery(session, condition, orderBy);
		if (relationships != null && relationships.length > 0)
			return relationships[0];
		else
			return null;
	}
	
	public static Relationship loadRelationshipByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Relationship[] relationships = listRelationshipByQuery(session, condition, orderBy, lockMode);
		if (relationships != null && relationships.length > 0)
			return relationships[0];
		else
			return null;
	}
	
	/*public static java.util.Iterator iterateRelationshipByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateRelationshipByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static java.util.Iterator iterateRelationshipByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateRelationshipByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static java.util.Iterator iterateRelationshipByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Relationship as Relationship");
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
	
	public static java.util.Iterator iterateRelationshipByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Relationship as Relationship");
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
	
	public static Relationship createRelationship() {
		return new Relationship();
	}
	
	/*public static boolean save(Relationship relationship) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, relationship);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Relationship relationship) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, relationship);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Relationship relationship)throws PersistentException {
		try {
			WorkingSet[] lWorkingSets = (WorkingSet[])relationship.getWorkingSet().toArray(new WorkingSet[relationship.getWorkingSet().size()]);
			for(int i = 0; i < lWorkingSets.length; i++) {
				lWorkingSets[i].setRelationship(null);
			}
			return delete(relationship);
		}
		catch(Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static boolean deleteAndDissociate(Relationship relationship, Session session)throws PersistentException {
		try {
			WorkingSet[] lWorkingSets = (WorkingSet[])relationship.getWorkingSet().toArray(new WorkingSet[relationship.getWorkingSet().size()]);
			for(int i = 0; i < lWorkingSets.length; i++) {
				lWorkingSets[i].setRelationship(null);
			}
			try {
				session.delete(relationship);
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
	
	/*public static boolean refresh(Relationship relationship) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(relationship);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Relationship relationship) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(relationship);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static Relationship loadRelationshipByCriteria(RelationshipCriteria relationshipCriteria) {
		Relationship[] relationships = listRelationshipByCriteria(relationshipCriteria);
		if(relationships == null || relationships.length == 0) {
			return null;
		}
		return relationships[0];
	}
	
	public static Relationship[] listRelationshipByCriteria(RelationshipCriteria relationshipCriteria) {
		return relationshipCriteria.listRelationship();
	}
}
