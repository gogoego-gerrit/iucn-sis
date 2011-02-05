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
import org.iucn.sis.shared.api.models.Infratype;

public class InfratypeDAO {
	/*public static Infratype[] listInfratypeByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listInfratypeByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Infratype[] listInfratypeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listInfratypeByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Infratype[] listInfratypeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Infratype as Infratype");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Infratype[]) list.toArray(new Infratype[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Infratype[] listInfratypeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Infratype as Infratype");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Infratype[]) list.toArray(new Infratype[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Infratype loadInfratypeByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadInfratypeByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Infratype loadInfratypeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadInfratypeByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Infratype loadInfratypeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Infratype[] infratypes = listInfratypeByQuery(session, condition, orderBy);
		if (infratypes != null && infratypes.length > 0)
			return infratypes[0];
		else
			return null;
	}
	
	public static Infratype loadInfratypeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Infratype[] infratypes = listInfratypeByQuery(session, condition, orderBy, lockMode);
		if (infratypes != null && infratypes.length > 0)
			return infratypes[0];
		else
			return null;
	}
	
	/*public static java.util.Iterator iterateInfratypeByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateInfratypeByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static java.util.Iterator iterateInfratypeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateInfratypeByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static java.util.Iterator iterateInfratypeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Infratype as Infratype");
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
	
	public static java.util.Iterator iterateInfratypeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Infratype as Infratype");
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
	
	public static Infratype createInfratype() {
		return new Infratype();
	}
	
	/*public static boolean save(Infratype infratype) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, infratype);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Infratype infratype) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, infratype);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Infratype infratype)throws PersistentException {
//		try {
//			if(infratype.getTaxon() != null) {
//				infratype.getTaxon().setInfratype(null);
//			}
//			
//			return delete(infratype);
//		}
//		catch(Exception e) {
//			Debug.println(e);
//			throw new PersistentException(e);
//		}
		return false;
	}
	
	public static boolean deleteAndDissociate(Infratype infratype, Session session)throws PersistentException {
//		try {
//			if(infratype.getTaxon() != null) {
//				infratype.getTaxon().setInfratype(null);
//			}
//			
//			try {
//				session.delete(infratype);
//				return true;
//			} catch (Exception e) {
//				return false;
//			}
//		}
//		catch(Exception e) {
//			Debug.println(e);
//			throw new PersistentException(e);
//		}
		return false;
	}
	
	public static boolean refresh(Infratype infratype) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(infratype);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Infratype infratype) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(infratype);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static Infratype loadInfratypeByCriteria(InfratypeCriteria infratypeCriteria) {
		Infratype[] infratypes = listInfratypeByCriteria(infratypeCriteria);
		if(infratypes == null || infratypes.length == 0) {
			return null;
		}
		return infratypes[0];
	}
	
	public static Infratype[] listInfratypeByCriteria(InfratypeCriteria infratypeCriteria) {
		return infratypeCriteria.listInfratype();
	}
}
