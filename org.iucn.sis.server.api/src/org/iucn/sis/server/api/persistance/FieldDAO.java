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
import org.iucn.sis.shared.api.models.Field;

public class FieldDAO {
	public static Field loadFieldByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadFieldByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field getFieldByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getFieldByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field loadFieldByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadFieldByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field getFieldByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getFieldByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field loadFieldByORMID(Session session, int id) throws PersistentException {
		try {
			return (Field) session.load(Field.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field getFieldByORMID(Session session, int id) throws PersistentException {
		try {
			return (Field) session.get(Field.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field loadFieldByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Field) session.load(Field.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field getFieldByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Field) session.get(Field.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field[] listFieldByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field[] listFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
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
			e.printStackTrace();
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
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field loadFieldByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field loadFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
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
	
	public static java.util.Iterator iterateFieldByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateFieldByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateFieldByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateFieldByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
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
			e.printStackTrace();
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
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Field createField() {
		return new Field();
	}
	
	public static boolean save(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(field);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(field);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	/**
	public static boolean deleteAndDissociate(Field field)throws PersistentException {
		try {
			if(field.getAssessment() != null) {
				field.getAssessment().getField().remove(field);
			}
			
			if(field.getFields() != null) {
				field.getFields().getChildren().remove(field);
			}
			
			Notes[] lNotess = (Notes[])field.getNotes().toArray(new Notes[field.getNotes().size()]);
			for(int i = 0; i < lNotess.length; i++) {
				lNotess[i].getField().remove(field);
			}
			Field[] lParents = (Field[])field.getChildren().toArray(new Field[field.getChildren().size()]);
			for(int i = 0; i < lParents.length; i++) {
				lParents[i].setFields(null);
			}
			Reference[] lReferences = (Reference[])field.getReference().toArray(new Reference[field.getReference().size()]);
			for(int i = 0; i < lReferences.length; i++) {
				lReferences[i].getField().remove(field);
			}
			PrimitiveField[] lPrimitiveFields = (PrimitiveField[])field.getPrimitiveField().toArray(new PrimitiveField[field.getPrimitiveField().size()]);
			for(int i = 0; i < lPrimitiveFields.length; i++) {
				lPrimitiveFields[i].setField(null);
			}
			return delete(field);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Field field, org.orm.Session session)throws PersistentException {
		try {
			if(field.getAssessment() != null) {
				field.getAssessment().getField().remove(field);
			}
			
			if(field.getFields() != null) {
				field.getFields().getChildren().remove(field);
			}
			
			Notes[] lNotess = (Notes[])field.getNotes().toArray(new Notes[field.getNotes().size()]);
			for(int i = 0; i < lNotess.length; i++) {
				lNotess[i].getField().remove(field);
			}
			Field[] lParents = (Field[])field.getChildren().toArray(new Field[field.getChildren().size()]);
			for(int i = 0; i < lParents.length; i++) {
				lParents[i].setFields(null);
			}
			Reference[] lReferences = (Reference[])field.getReference().toArray(new Reference[field.getReference().size()]);
			for(int i = 0; i < lReferences.length; i++) {
				lReferences[i].getField().remove(field);
			}
			PrimitiveField[] lPrimitiveFields = (PrimitiveField[])field.getPrimitiveField().toArray(new PrimitiveField[field.getPrimitiveField().size()]);
			for(int i = 0; i < lPrimitiveFields.length; i++) {
				lPrimitiveFields[i].setField(null);
			}
			try {
				session.delete(field);
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
	
	**/
	
	public static boolean refresh(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(field);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Field field) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(field);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
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
