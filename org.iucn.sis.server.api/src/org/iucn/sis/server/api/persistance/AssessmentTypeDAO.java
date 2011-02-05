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
import org.iucn.sis.shared.api.models.AssessmentType;

public class AssessmentTypeDAO {
	
	/*public static AssessmentType loadAssessmentTypeByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadAssessmentTypeByORMID(session, id);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static AssessmentType getAssessmentTypeByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getAssessmentTypeByORMID(session, id);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static AssessmentType loadAssessmentTypeByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadAssessmentTypeByORMID(session, id, lockMode);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static AssessmentType getAssessmentTypeByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getAssessmentTypeByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static AssessmentType loadAssessmentTypeByORMID(Session session, int id) throws PersistentException {
		try {
			return (AssessmentType) session.load(AssessmentType.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static AssessmentType getAssessmentTypeByORMID(Session session, int id) throws PersistentException {
		try {
			return (AssessmentType) session.get(AssessmentType.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static AssessmentType loadAssessmentTypeByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (AssessmentType) session.load(AssessmentType.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static AssessmentType getAssessmentTypeByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (AssessmentType) session.get(AssessmentType.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static AssessmentType[] listAssessmentTypeByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listAssessmentTypeByQuery(session, condition, orderBy);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static AssessmentType[] listAssessmentTypeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listAssessmentTypeByQuery(session, condition, orderBy, lockMode);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static AssessmentType[] listAssessmentTypeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From AssessmentType as AssessmentType");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (AssessmentType[]) list.toArray(new AssessmentType[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static AssessmentType[] listAssessmentTypeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From AssessmentType as AssessmentType");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (AssessmentType[]) list.toArray(new AssessmentType[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static AssessmentType loadAssessmentTypeByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadAssessmentTypeByQuery(session, condition, orderBy);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static AssessmentType loadAssessmentTypeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadAssessmentTypeByQuery(session, condition, orderBy, lockMode);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static AssessmentType loadAssessmentTypeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		AssessmentType[] assessmentTypes = listAssessmentTypeByQuery(session, condition, orderBy);
		if (assessmentTypes != null && assessmentTypes.length > 0)
			return assessmentTypes[0];
		else
			return null;
	}
	
	public static AssessmentType loadAssessmentTypeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		AssessmentType[] assessmentTypes = listAssessmentTypeByQuery(session, condition, orderBy, lockMode);
		if (assessmentTypes != null && assessmentTypes.length > 0)
			return assessmentTypes[0];
		else
			return null;
	}
	
	/*public static java.util.Iterator iterateAssessmentTypeByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateAssessmentTypeByQuery(session, condition, orderBy);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static java.util.Iterator iterateAssessmentTypeByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateAssessmentTypeByQuery(session, condition, orderBy, lockMode);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static java.util.Iterator iterateAssessmentTypeByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From AssessmentType as AssessmentType");
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
	
	public static java.util.Iterator iterateAssessmentTypeByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From AssessmentType as AssessmentType");
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
	
	public static AssessmentType createAssessmentType() {
		return new AssessmentType();
	}
	
	/*public static boolean save(AssessmentType assessmentType) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, assessmentType);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(AssessmentType assessmentType) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, assessmentType);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(AssessmentType assessmentType)throws PersistentException {
		try {
			WorkingSet[] lWorkingSets = (WorkingSet[])assessmentType.getWorkingSet().toArray(new WorkingSet[assessmentType.getWorkingSet().size()]);
			for(int i = 0; i < lWorkingSets.length; i++) {
				lWorkingSets[i].setAssessment_type(null);
			}
			Assessment[] lAssessments = (Assessment[])assessmentType.getAssessment().toArray(new Assessment[assessmentType.getAssessment().size()]);
			for(int i = 0; i < lAssessments.length; i++) {
				lAssessments[i].setAssessmentType(null);
			}
			return delete(assessmentType);
		}
		catch(Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(AssessmentType assessmentType, org.orm.Session session)throws PersistentException {
		try {
			WorkingSet[] lWorkingSets = (WorkingSet[])assessmentType.getWorkingSet().toArray(new WorkingSet[assessmentType.getWorkingSet().size()]);
			for(int i = 0; i < lWorkingSets.length; i++) {
				lWorkingSets[i].setAssessment_type(null);
			}
			Assessment[] lAssessments = (Assessment[])assessmentType.getAssessment().toArray(new Assessment[assessmentType.getAssessment().size()]);
			for(int i = 0; i < lAssessments.length; i++) {
				lAssessments[i].setAssessmentType(null);
			}
			try {
				session.delete(assessmentType);
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
	
	
	public static boolean refresh(AssessmentType assessmentType) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(assessmentType);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(AssessmentType assessmentType) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(assessmentType);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static AssessmentType loadAssessmentTypeByCriteria(AssessmentTypeCriteria assessmentTypeCriteria) {
		AssessmentType[] assessmentTypes = listAssessmentTypeByCriteria(assessmentTypeCriteria);
		if(assessmentTypes == null || assessmentTypes.length == 0) {
			return null;
		}
		return assessmentTypes[0];
	}
	
	public static AssessmentType[] listAssessmentTypeByCriteria(AssessmentTypeCriteria assessmentTypeCriteria) {
		return assessmentTypeCriteria.listAssessmentType();
	}
}
