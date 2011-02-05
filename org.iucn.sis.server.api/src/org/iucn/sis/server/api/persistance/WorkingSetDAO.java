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
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;

public class WorkingSetDAO {
	/*public static WorkingSet loadWorkingSetByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadWorkingSetByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static WorkingSet getWorkingSetByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getWorkingSetByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static WorkingSet loadWorkingSetByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadWorkingSetByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static WorkingSet getWorkingSetByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getWorkingSetByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static WorkingSet loadWorkingSetByORMID(Session session, int id) throws PersistentException {
		try {
			return (WorkingSet) session.load(WorkingSet.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static WorkingSet getWorkingSetByORMID(Session session, int id) throws PersistentException {
		try {
			return (WorkingSet) session.get(WorkingSet.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static WorkingSet loadWorkingSetByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (WorkingSet) session.load(WorkingSet.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static WorkingSet getWorkingSetByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (WorkingSet) session.get(WorkingSet.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static WorkingSet[] listWorkingSetByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listWorkingSetByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static WorkingSet[] listWorkingSetByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listWorkingSetByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static WorkingSet[] listWorkingSetByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From WorkingSet as WorkingSet");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (WorkingSet[]) list.toArray(new WorkingSet[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static WorkingSet[] listWorkingSetByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From WorkingSet as WorkingSet");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (WorkingSet[]) list.toArray(new WorkingSet[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static WorkingSet loadWorkingSetByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadWorkingSetByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static WorkingSet loadWorkingSetByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadWorkingSetByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static WorkingSet loadWorkingSetByQuery(Session session, String condition, String orderBy) throws PersistentException {
		WorkingSet[] workingSets = listWorkingSetByQuery(session, condition, orderBy);
		if (workingSets != null && workingSets.length > 0)
			return workingSets[0];
		else
			return null;
	}
	
	public static WorkingSet loadWorkingSetByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		WorkingSet[] workingSets = listWorkingSetByQuery(session, condition, orderBy, lockMode);
		if (workingSets != null && workingSets.length > 0)
			return workingSets[0];
		else
			return null;
	}
	
	/*public static java.util.Iterator iterateWorkingSetByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateWorkingSetByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static java.util.Iterator iterateWorkingSetByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateWorkingSetByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static java.util.Iterator iterateWorkingSetByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From WorkingSet as WorkingSet");
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
	
	public static java.util.Iterator iterateWorkingSetByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From WorkingSet as WorkingSet");
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
	
	public static WorkingSet createWorkingSet() {
		return new WorkingSet();
	}
	
	/*public static boolean save(WorkingSet workingSet) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, workingSet);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(WorkingSet workingSet) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, workingSet);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(WorkingSet workingSet)throws PersistentException {
		try {
			if(workingSet.getRelationship() != null) {
				workingSet.getRelationship().getWorkingSet().remove(workingSet);
			}
			
			
			if(workingSet.getCreator() != null) {
				workingSet.getCreator().getOwnedWorkingSets().remove(workingSet);
			}
			
			Taxon[] lTaxons = (Taxon[])workingSet.getTaxon().toArray(new Taxon[workingSet.getTaxon().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getWorking_set().remove(workingSet);
			}
			Region[] lRegions = (Region[])workingSet.getRegion().toArray(new Region[workingSet.getRegion().size()]);
			for(int i = 0; i < lRegions.length; i++) {
				lRegions[i].getWorking_set().remove(workingSet);
			}
			Edit[] lEdits = (Edit[])workingSet.getEdit().toArray(new Edit[workingSet.getEdit().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].getWorking_set().remove(workingSet);
			}
			User[] lUsers = (User[])workingSet.getUsers().toArray(new User[workingSet.getUsers().size()]);
			for(int i = 0; i < lUsers.length; i++) {
				lUsers[i].getSubscribedWorkingSets().remove(workingSet);
			}
			return delete(workingSet);
		}
		catch(Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static boolean deleteAndDissociate(WorkingSet workingSet, Session session)throws PersistentException {
		try {
			if(workingSet.getRelationship() != null) {
				workingSet.getRelationship().getWorkingSet().remove(workingSet);
			}
			
			
			if(workingSet.getCreator() != null) {
				workingSet.getCreator().getOwnedWorkingSets().remove(workingSet);
			}
			
			
			Taxon[] lTaxons = (Taxon[])workingSet.getTaxon().toArray(new Taxon[workingSet.getTaxon().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getWorking_set().remove(workingSet);
			}
			Region[] lRegions = (Region[])workingSet.getRegion().toArray(new Region[workingSet.getRegion().size()]);
			for(int i = 0; i < lRegions.length; i++) {
				lRegions[i].getWorking_set().remove(workingSet);
			}
			Edit[] lEdits = (Edit[])workingSet.getEdit().toArray(new Edit[workingSet.getEdit().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].getWorking_set().remove(workingSet);
			}
			User[] lUsers = (User[])workingSet.getUsers().toArray(new User[workingSet.getUsers().size()]);
			for(int i = 0; i < lUsers.length; i++) {
				lUsers[i].getSubscribedWorkingSets().remove(workingSet);
			}
			try {
				session.delete(workingSet);
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
	
	/*public static boolean refresh(WorkingSet workingSet) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(workingSet);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(WorkingSet workingSet) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(workingSet);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static WorkingSet loadWorkingSetByCriteria(WorkingSetCriteria workingSetCriteria) {
		WorkingSet[] workingSets = listWorkingSetByCriteria(workingSetCriteria);
		if(workingSets == null || workingSets.length == 0) {
			return null;
		}
		return workingSets[0];
	}
	
	public static WorkingSet[] listWorkingSetByCriteria(WorkingSetCriteria workingSetCriteria) {
		return workingSetCriteria.listWorkingSet();
	}
}
