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
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

public class TaxonLevelDAO {
	public static TaxonLevel loadTaxonLevelByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonLevelByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel getTaxonLevelByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getTaxonLevelByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel loadTaxonLevelByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonLevelByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel getTaxonLevelByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getTaxonLevelByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel loadTaxonLevelByORMID(Session session, int id) throws PersistentException {
		try {
			return (TaxonLevel) session.load(TaxonLevel.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel getTaxonLevelByORMID(Session session, int id) throws PersistentException {
		try {
			return (TaxonLevel) session.get(TaxonLevel.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel loadTaxonLevelByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (TaxonLevel) session.load(TaxonLevel.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel getTaxonLevelByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (TaxonLevel) session.get(TaxonLevel.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel[] listTaxonLevelByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listTaxonLevelByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel[] listTaxonLevelByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listTaxonLevelByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel[] listTaxonLevelByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonLevel as TaxonLevel");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (TaxonLevel[]) list.toArray(new TaxonLevel[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel[] listTaxonLevelByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonLevel as TaxonLevel");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (TaxonLevel[]) list.toArray(new TaxonLevel[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel loadTaxonLevelByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonLevelByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel loadTaxonLevelByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonLevelByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel loadTaxonLevelByQuery(Session session, String condition, String orderBy) throws PersistentException {
		TaxonLevel[] taxonLevels = listTaxonLevelByQuery(session, condition, orderBy);
		if (taxonLevels != null && taxonLevels.length > 0)
			return taxonLevels[0];
		else
			return null;
	}
	
	public static TaxonLevel loadTaxonLevelByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		TaxonLevel[] taxonLevels = listTaxonLevelByQuery(session, condition, orderBy, lockMode);
		if (taxonLevels != null && taxonLevels.length > 0)
			return taxonLevels[0];
		else
			return null;
	}
	
	public static java.util.Iterator iterateTaxonLevelByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateTaxonLevelByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateTaxonLevelByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateTaxonLevelByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateTaxonLevelByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonLevel as TaxonLevel");
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
	
	public static java.util.Iterator iterateTaxonLevelByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonLevel as TaxonLevel");
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
	
	public static TaxonLevel createTaxonLevel() {
		return new TaxonLevel();
	}
	
	public static boolean save(TaxonLevel taxonLevel) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(taxonLevel);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(TaxonLevel taxonLevel) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(taxonLevel);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(TaxonLevel taxonLevel)throws PersistentException {
		try {
			Taxon[] lTaxas = (Taxon[])taxonLevel.getTaxa().toArray(new Taxon[taxonLevel.getTaxa().size()]);
			for(int i = 0; i < lTaxas.length; i++) {
				lTaxas[i].setTaxonLevel(null);
			}
			Synonym[] lSynonyms = (Synonym[])taxonLevel.getSynonyms().toArray(new Synonym[taxonLevel.getSynonyms().size()]);
			for(int i = 0; i < lSynonyms.length; i++) {
				lSynonyms[i].setTaxon_level(null);
			}
			return delete(taxonLevel);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(TaxonLevel taxonLevel, Session session)throws PersistentException {
		try {
			Taxon[] lTaxas = (Taxon[])taxonLevel.getTaxa().toArray(new Taxon[taxonLevel.getTaxa().size()]);
			for(int i = 0; i < lTaxas.length; i++) {
				lTaxas[i].setTaxonLevel(null);
			}
			Synonym[] lSynonyms = (Synonym[])taxonLevel.getSynonyms().toArray(new Synonym[taxonLevel.getSynonyms().size()]);
			for(int i = 0; i < lSynonyms.length; i++) {
				lSynonyms[i].setTaxon_level(null);
			}
			try {
				session.delete(taxonLevel);
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
	
	public static boolean refresh(TaxonLevel taxonLevel) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(taxonLevel);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(TaxonLevel taxonLevel) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(taxonLevel);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonLevel loadTaxonLevelByCriteria(TaxonLevelCriteria taxonLevelCriteria) {
		TaxonLevel[] taxonLevels = listTaxonLevelByCriteria(taxonLevelCriteria);
		if(taxonLevels == null || taxonLevels.length == 0) {
			return null;
		}
		return taxonLevels[0];
	}
	
	public static TaxonLevel[] listTaxonLevelByCriteria(TaxonLevelCriteria taxonLevelCriteria) {
		return taxonLevelCriteria.listTaxonLevel();
	}
}
