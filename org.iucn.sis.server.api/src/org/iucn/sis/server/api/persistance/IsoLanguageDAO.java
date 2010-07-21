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
import org.iucn.sis.shared.api.models.IsoLanguage;

public class IsoLanguageDAO {
	public static IsoLanguage loadIsoLanguageByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadIsoLanguageByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage getIsoLanguageByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getIsoLanguageByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage loadIsoLanguageByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadIsoLanguageByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage getIsoLanguageByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getIsoLanguageByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage loadIsoLanguageByORMID(Session session, int id) throws PersistentException {
		try {
			return (IsoLanguage) session.load(IsoLanguage.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage getIsoLanguageByORMID(Session session, int id) throws PersistentException {
		try {
			return (IsoLanguage) session.get(IsoLanguage.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage loadIsoLanguageByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (IsoLanguage) session.load(IsoLanguage.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage getIsoLanguageByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (IsoLanguage) session.get(IsoLanguage.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage[] listIsoLanguageByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listIsoLanguageByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage[] listIsoLanguageByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listIsoLanguageByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage[] listIsoLanguageByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From IsoLanguage as IsoLanguage");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (IsoLanguage[]) list.toArray(new IsoLanguage[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage[] listIsoLanguageByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From IsoLanguage as IsoLanguage");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (IsoLanguage[]) list.toArray(new IsoLanguage[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage loadIsoLanguageByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadIsoLanguageByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage loadIsoLanguageByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadIsoLanguageByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage loadIsoLanguageByQuery(Session session, String condition, String orderBy) throws PersistentException {
		IsoLanguage[] isoLanguages = listIsoLanguageByQuery(session, condition, orderBy);
		if (isoLanguages != null && isoLanguages.length > 0)
			return isoLanguages[0];
		else
			return null;
	}
	
	public static IsoLanguage loadIsoLanguageByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		IsoLanguage[] isoLanguages = listIsoLanguageByQuery(session, condition, orderBy, lockMode);
		if (isoLanguages != null && isoLanguages.length > 0)
			return isoLanguages[0];
		else
			return null;
	}
	
	public static java.util.Iterator iterateIsoLanguageByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateIsoLanguageByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateIsoLanguageByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateIsoLanguageByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateIsoLanguageByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From IsoLanguage as IsoLanguage");
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
	
	public static java.util.Iterator iterateIsoLanguageByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From IsoLanguage as IsoLanguage");
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
	
	public static IsoLanguage createIsoLanguage() {
		return new IsoLanguage();
	}
	
	public static boolean save(IsoLanguage isoLanguage) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(isoLanguage);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(IsoLanguage isoLanguage) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(isoLanguage);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	/**
	public static boolean deleteAndDissociate(IsoLanguage isoLanguage)throws PersistentException {
		try {
			CommonName[] lCommonNames = (CommonName[])isoLanguage.getCommonName().toArray(new CommonName[isoLanguage.getCommonName().size()]);
			for(int i = 0; i < lCommonNames.length; i++) {
				lCommonNames[i].setIso_language(null);
			}
			return delete(isoLanguage);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(IsoLanguage isoLanguage, org.orm.Session session)throws PersistentException {
		try {
			CommonName[] lCommonNames = (CommonName[])isoLanguage.getCommonName().toArray(new CommonName[isoLanguage.getCommonName().size()]);
			for(int i = 0; i < lCommonNames.length; i++) {
				lCommonNames[i].setIso_language(null);
			}
			try {
				session.delete(isoLanguage);
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
	
	public static boolean refresh(IsoLanguage isoLanguage) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(isoLanguage);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(IsoLanguage isoLanguage) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(isoLanguage);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static IsoLanguage loadIsoLanguageByCriteria(IsoLanguageCriteria isoLanguageCriteria) {
		IsoLanguage[] isoLanguages = listIsoLanguageByCriteria(isoLanguageCriteria);
		if(isoLanguages == null || isoLanguages.length == 0) {
			return null;
		}
		return isoLanguages[0];
	}
	
	public static IsoLanguage[] listIsoLanguageByCriteria(IsoLanguageCriteria isoLanguageCriteria) {
		return isoLanguageCriteria.listIsoLanguage();
	}
}
