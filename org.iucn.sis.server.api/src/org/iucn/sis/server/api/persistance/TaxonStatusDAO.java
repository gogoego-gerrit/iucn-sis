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
import org.iucn.sis.shared.api.models.TaxonStatus;

public class TaxonStatusDAO {
	public static TaxonStatus loadTaxonStatusByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonStatusByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus getTaxonStatusByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getTaxonStatusByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus loadTaxonStatusByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonStatusByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus getTaxonStatusByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getTaxonStatusByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus loadTaxonStatusByORMID(Session session, int id) throws PersistentException {
		try {
			return (TaxonStatus) session.load(TaxonStatus.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus getTaxonStatusByORMID(Session session, int id) throws PersistentException {
		try {
			return (TaxonStatus) session.get(TaxonStatus.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus loadTaxonStatusByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (TaxonStatus) session.load(TaxonStatus.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus getTaxonStatusByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (TaxonStatus) session.get(TaxonStatus.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus[] listTaxonStatusByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listTaxonStatusByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus[] listTaxonStatusByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listTaxonStatusByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus[] listTaxonStatusByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonStatus as TaxonStatus");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (TaxonStatus[]) list.toArray(new TaxonStatus[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus[] listTaxonStatusByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonStatus as TaxonStatus");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (TaxonStatus[]) list.toArray(new TaxonStatus[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus loadTaxonStatusByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonStatusByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus loadTaxonStatusByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadTaxonStatusByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus loadTaxonStatusByQuery(Session session, String condition, String orderBy) throws PersistentException {
		TaxonStatus[] taxonStatuses = listTaxonStatusByQuery(session, condition, orderBy);
		if (taxonStatuses != null && taxonStatuses.length > 0)
			return taxonStatuses[0];
		else
			return null;
	}
	
	public static TaxonStatus loadTaxonStatusByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		TaxonStatus[] taxonStatuses = listTaxonStatusByQuery(session, condition, orderBy, lockMode);
		if (taxonStatuses != null && taxonStatuses.length > 0)
			return taxonStatuses[0];
		else
			return null;
	}
	
	public static java.util.Iterator iterateTaxonStatusByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateTaxonStatusByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateTaxonStatusByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateTaxonStatusByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateTaxonStatusByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonStatus as TaxonStatus");
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
	
	public static java.util.Iterator iterateTaxonStatusByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From TaxonStatus as TaxonStatus");
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
	
	public static TaxonStatus createTaxonStatus() {
		return new TaxonStatus();
	}
	
	public static boolean save(TaxonStatus taxonStatus) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(taxonStatus);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(TaxonStatus taxonStatus) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(taxonStatus);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(TaxonStatus taxonStatus)throws PersistentException {
		try {
			Taxon[] lTaxas = (Taxon[])taxonStatus.getTaxa().toArray(new Taxon[taxonStatus.getTaxa().size()]);
			for(int i = 0; i < lTaxas.length; i++) {
				lTaxas[i].setTaxonStatus(null);
			}
			
			return delete(taxonStatus);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(TaxonStatus taxonStatus, Session session)throws PersistentException {
		try {
			Taxon[] lTaxas = (Taxon[])taxonStatus.getTaxa().toArray(new Taxon[taxonStatus.getTaxa().size()]);
			for(int i = 0; i < lTaxas.length; i++) {
				lTaxas[i].setTaxonStatus(null);
			}
			
			try {
				session.delete(taxonStatus);
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
	
	public static boolean refresh(TaxonStatus taxonStatus) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(taxonStatus);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(TaxonStatus taxonStatus) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(taxonStatus);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static TaxonStatus loadTaxonStatusByCriteria(TaxonStatusCriteria taxonStatusCriteria) {
		TaxonStatus[] taxonStatuses = listTaxonStatusByCriteria(taxonStatusCriteria);
		if(taxonStatuses == null || taxonStatuses.length == 0) {
			return null;
		}
		return taxonStatuses[0];
	}
	
	public static TaxonStatus[] listTaxonStatusByCriteria(TaxonStatusCriteria taxonStatusCriteria) {
		return taxonStatusCriteria.listTaxonStatus();
	}
}
