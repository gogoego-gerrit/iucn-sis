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
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.WorkingSet;

public class RegionDAO {
	public static Region loadRegionByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadRegionByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region getRegionByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getRegionByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region loadRegionByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadRegionByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region getRegionByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getRegionByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region loadRegionByORMID(Session session, int id) throws PersistentException {
		try {
			return (Region) session.load(Region.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region getRegionByORMID(Session session, int id) throws PersistentException {
		try {
			return (Region) session.get(Region.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region loadRegionByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Region) session.load(Region.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region getRegionByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Region) session.get(Region.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region[] listRegionByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listRegionByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region[] listRegionByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listRegionByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region[] listRegionByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Region as Region");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Region[]) list.toArray(new Region[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region[] listRegionByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Region as Region");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Region[]) list.toArray(new Region[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region loadRegionByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadRegionByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region loadRegionByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadRegionByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region loadRegionByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Region[] regions = listRegionByQuery(session, condition, orderBy);
		if (regions != null && regions.length > 0)
			return regions[0];
		else
			return null;
	}
	
	public static Region loadRegionByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Region[] regions = listRegionByQuery(session, condition, orderBy, lockMode);
		if (regions != null && regions.length > 0)
			return regions[0];
		else
			return null;
	}
	
	public static java.util.Iterator iterateRegionByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateRegionByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateRegionByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateRegionByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateRegionByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Region as Region");
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
	
	public static java.util.Iterator iterateRegionByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Region as Region");
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
	
	public static Region createRegion() {
		return new Region();
	}
	
	public static boolean save(Region region) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(region);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Region region) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(region);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Region region)throws PersistentException {
		try {
			WorkingSet[] lWorking_sets = (WorkingSet[])region.getWorking_set().toArray(new WorkingSet[region.getWorking_set().size()]);
			for(int i = 0; i < lWorking_sets.length; i++) {
				lWorking_sets[i].getRegion().remove(region);
			}
			return delete(region);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Region region, Session session)throws PersistentException {
		try {
			WorkingSet[] lWorking_sets = (WorkingSet[])region.getWorking_set().toArray(new WorkingSet[region.getWorking_set().size()]);
			for(int i = 0; i < lWorking_sets.length; i++) {
				lWorking_sets[i].getRegion().remove(region);
			}
			try {
				session.delete(region);
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
	
	public static boolean refresh(Region region) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(region);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Region region) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(region);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Region loadRegionByCriteria(RegionCriteria regionCriteria) {
		Region[] regions = listRegionByCriteria(regionCriteria);
		if(regions == null || regions.length == 0) {
			return null;
		}
		return regions[0];
	}
	
	public static Region[] listRegionByCriteria(RegionCriteria regionCriteria) {
		return regionCriteria.listRegion();
	}
}
