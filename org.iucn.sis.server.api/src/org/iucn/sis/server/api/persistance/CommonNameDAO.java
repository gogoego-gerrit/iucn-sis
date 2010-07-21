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
import org.iucn.sis.shared.api.models.CommonName;

public class CommonNameDAO {
	public static CommonName loadCommonNameByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadCommonNameByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName getCommonNameByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getCommonNameByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName loadCommonNameByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadCommonNameByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName getCommonNameByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getCommonNameByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName loadCommonNameByORMID(Session session, int id) throws PersistentException {
		try {
			return (CommonName) session.load(CommonName.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName getCommonNameByORMID(Session session, int id) throws PersistentException {
		try {
			return (CommonName) session.get(CommonName.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName loadCommonNameByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (CommonName) session.load(CommonName.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName getCommonNameByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (CommonName) session.get(CommonName.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName[] listCommonNameByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listCommonNameByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName[] listCommonNameByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listCommonNameByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName[] listCommonNameByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From CommonName as CommonName");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (CommonName[]) list.toArray(new CommonName[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName[] listCommonNameByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From CommonName as CommonName");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (CommonName[]) list.toArray(new CommonName[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName loadCommonNameByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadCommonNameByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName loadCommonNameByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadCommonNameByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName loadCommonNameByQuery(Session session, String condition, String orderBy) throws PersistentException {
		CommonName[] commonNames = listCommonNameByQuery(session, condition, orderBy);
		if (commonNames != null && commonNames.length > 0)
			return commonNames[0];
		else
			return null;
	}
	
	public static CommonName loadCommonNameByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		CommonName[] commonNames = listCommonNameByQuery(session, condition, orderBy, lockMode);
		if (commonNames != null && commonNames.length > 0)
			return commonNames[0];
		else
			return null;
	}
	
	public static java.util.Iterator iterateCommonNameByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateCommonNameByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateCommonNameByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateCommonNameByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateCommonNameByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From CommonName as CommonName");
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
	
	public static java.util.Iterator iterateCommonNameByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From CommonName as CommonName");
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
	
	public static CommonName createCommonName() {
		return new CommonName();
	}
	
	public static boolean save(CommonName commonName) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(commonName);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(CommonName commonName) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(commonName);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	/**
	public static boolean deleteAndDissociate(CommonName commonName)throws PersistentException {
		try {
			if(commonName.getIso_language() != null) {
				commonName.getIso_language().getCommonName().remove(commonName);
			}
			
			if(commonName.getTaxon() != null) {
				commonName.getTaxon().getCommonNames().remove(commonName);
			}
			
			Reference[] lReferences = (Reference[])commonName.getReference().toArray(new Reference[commonName.getReference().size()]);
			for(int i = 0; i < lReferences.length; i++) {
				lReferences[i].getCommon_name().remove(commonName);
			}
			Notes[] lNotess = (Notes[])commonName.getNotes().toArray(new Notes[commonName.getNotes().size()]);
			for(int i = 0; i < lNotess.length; i++) {
				lNotess[i].getCommonName().remove(commonName);
			}
			return delete(commonName);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(CommonName commonName, org.orm.Session session)throws PersistentException {
		try {
			if(commonName.getIso_language() != null) {
				commonName.getIso_language().getCommonName().remove(commonName);
			}
			
			if(commonName.getTaxon() != null) {
				commonName.getTaxon().getCommonNames().remove(commonName);
			}
			
			Reference[] lReferences = (Reference[])commonName.getReference().toArray(new Reference[commonName.getReference().size()]);
			for(int i = 0; i < lReferences.length; i++) {
				lReferences[i].getCommon_name().remove(commonName);
			}
			Notes[] lNotess = (Notes[])commonName.getNotes().toArray(new Notes[commonName.getNotes().size()]);
			for(int i = 0; i < lNotess.length; i++) {
				lNotess[i].getCommonName().remove(commonName);
			}
			try {
				session.delete(commonName);
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
	
	public static boolean refresh(CommonName commonName) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(commonName);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(CommonName commonName) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(commonName);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static CommonName loadCommonNameByCriteria(CommonNameCriteria commonNameCriteria) {
		CommonName[] commonNames = listCommonNameByCriteria(commonNameCriteria);
		if(commonNames == null || commonNames.length == 0) {
			return null;
		}
		return commonNames[0];
	}
	
	public static CommonName[] listCommonNameByCriteria(CommonNameCriteria commonNameCriteria) {
		return commonNameCriteria.listCommonName();
	}
}
