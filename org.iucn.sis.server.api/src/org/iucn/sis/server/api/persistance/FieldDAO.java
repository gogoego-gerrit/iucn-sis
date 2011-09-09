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
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;

public class FieldDAO {
	/*public static Field loadFieldByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadFieldByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Field getFieldByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getFieldByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Field loadFieldByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadFieldByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Field getFieldByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getFieldByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Field loadFieldByORMID(Session session, int id) throws PersistentException {
		try {
			return (Field) session.load(Field.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Field getFieldByORMID(Session session, int id) throws PersistentException {
		try {
			return (Field) session.get(Field.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Field loadFieldByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Field) session.load(Field.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Field getFieldByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Field) session.get(Field.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Field[] listFieldByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Field[] listFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Field[] listFieldByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Field as Field");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Field[]) list.toArray(new Field[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Field[] listFieldByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Field as Field");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Field[]) list.toArray(new Field[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Field loadFieldByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Field loadFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Field loadFieldByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Field[] fields = listFieldByQuery(session, condition, orderBy);
		if (fields != null && fields.length > 0)
			return fields[0];
		else
			return null;
	}
	
	public static Field loadFieldByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Field[] fields = listFieldByQuery(session, condition, orderBy, lockMode);
		if (fields != null && fields.length > 0)
			return fields[0];
		else
			return null;
	}
	
	/*public static java.util.Iterator iterateFieldByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static java.util.Iterator iterateFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static java.util.Iterator iterateFieldByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Field as Field");
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
	
	public static java.util.Iterator iterateFieldByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Field as Field");
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
	
	public static Field createField() {
		return new Field();
	}
	
	/*public static boolean save(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, field);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, field);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static boolean deleteAndDissociate(Field field, Session session)throws PersistentException {
		try {
			if(field.getAssessment() != null) {
				field.getAssessment().getField().remove(field);
			}
			
			if (field.getParent() != null) {
				field.getParent().getFields().remove(field);
				field.setParent(null);
			}
			
			if(field.getFields() != null) {
				Field[] lFields = (Field[])field.getFields().toArray(new Field[field.getFields().size()]);
				for (int i = 0; i < lFields.length; i++) {
					field.getFields().remove(lFields[i]);
					FieldDAO.deleteAndDissociate(lFields[i], session);
				}
			}
			
			Notes[] lNotess = (Notes[])field.getNotes().toArray(new Notes[field.getNotes().size()]);
			for(int i = 0; i < lNotess.length; i++) {
				lNotess[i].getFields().remove(field);
			}
			
			Reference[] lReferences = (Reference[])field.getReference().toArray(new Reference[field.getReference().size()]);
			for(int i = 0; i < lReferences.length; i++) {
				lReferences[i].getField().remove(field);
			}
			PrimitiveField[] lPrimitiveFields = (PrimitiveField[])field.getPrimitiveField().toArray(new PrimitiveField[field.getPrimitiveField().size()]);
			for(int i = 0; i < lPrimitiveFields.length; i++) {
				PrimitiveFieldDAO.deleteAndDissociate(lPrimitiveFields[i], session);
			}
			try {
				session.delete(field);
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
	
	
	/*public static boolean refresh(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(field);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(field);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static Field loadFieldByCriteria(FieldCriteria fieldCriteria) {
		Field[] fields = listFieldByCriteria(fieldCriteria);
		if(fields == null || fields.length == 0) {
			return null;
		}
		return fields[0];
	}
	
	public static Field[] listFieldByCriteria(FieldCriteria fieldCriteria) {
		return fieldCriteria.listField();
	}
}
