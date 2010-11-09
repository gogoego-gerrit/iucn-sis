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
import org.iucn.sis.shared.api.models.PrimitiveField;

public class PrimitiveFieldDAO {
	public static PrimitiveField loadPrimitiveFieldByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPrimitiveFieldByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField getPrimitiveFieldByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getPrimitiveFieldByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField loadPrimitiveFieldByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPrimitiveFieldByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField getPrimitiveFieldByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getPrimitiveFieldByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField loadPrimitiveFieldByORMID(Session session, int id) throws PersistentException {
		try {
			return (PrimitiveField) session.load(PrimitiveField.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField getPrimitiveFieldByORMID(Session session, int id) throws PersistentException {
		try {
			return (PrimitiveField) session.get(PrimitiveField.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField loadPrimitiveFieldByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (PrimitiveField) session.load(PrimitiveField.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField getPrimitiveFieldByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (PrimitiveField) session.get(PrimitiveField.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField[] listPrimitiveFieldByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listPrimitiveFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField[] listPrimitiveFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listPrimitiveFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField[] listPrimitiveFieldByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PrimitiveField as PrimitiveField");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (PrimitiveField[]) list.toArray(new PrimitiveField[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField[] listPrimitiveFieldByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PrimitiveField as PrimitiveField");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (PrimitiveField[]) list.toArray(new PrimitiveField[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField loadPrimitiveFieldByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPrimitiveFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField loadPrimitiveFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadPrimitiveFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField loadPrimitiveFieldByQuery(Session session, String condition, String orderBy) throws PersistentException {
		PrimitiveField[] primitiveFields = listPrimitiveFieldByQuery(session, condition, orderBy);
		if (primitiveFields != null && primitiveFields.length > 0)
			return primitiveFields[0];
		else
			return null;
	}
	
	public static PrimitiveField loadPrimitiveFieldByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		PrimitiveField[] primitiveFields = listPrimitiveFieldByQuery(session, condition, orderBy, lockMode);
		if (primitiveFields != null && primitiveFields.length > 0)
			return primitiveFields[0];
		else
			return null;
	}
	
	public static java.util.Iterator iteratePrimitiveFieldByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iteratePrimitiveFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iteratePrimitiveFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iteratePrimitiveFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iteratePrimitiveFieldByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PrimitiveField as PrimitiveField");
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
	
	public static java.util.Iterator iteratePrimitiveFieldByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From PrimitiveField as PrimitiveField");
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
	
	
	public static boolean save(PrimitiveField primitiveField) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(primitiveField);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(PrimitiveField primitiveField) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(primitiveField);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(PrimitiveField primitiveField)throws PersistentException {
		try {
			if(primitiveField.getField() != null) {
				primitiveField.getField().getPrimitiveField().remove(primitiveField);
			}
			
			return delete(primitiveField);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	
	
	
	public static boolean refresh(PrimitiveField primitiveField) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(primitiveField);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(PrimitiveField primitiveField) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(primitiveField);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static PrimitiveField loadPrimitiveFieldByCriteria(PrimitiveFieldCriteria primitiveFieldCriteria) {
		PrimitiveField[] primitiveFields = listPrimitiveFieldByCriteria(primitiveFieldCriteria);
		if(primitiveFields == null || primitiveFields.length == 0) {
			return null;
		}
		return primitiveFields[0];
	}
	
	public static PrimitiveField[] listPrimitiveFieldByCriteria(PrimitiveFieldCriteria primitiveFieldCriteria) {
		return primitiveFieldCriteria.listPrimitiveField();
	}
}
