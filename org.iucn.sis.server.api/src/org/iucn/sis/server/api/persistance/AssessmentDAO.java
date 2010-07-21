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
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;

public class AssessmentDAO {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	
	
	public static Assessment getAssessment(int id) throws PersistentException {
		Assessment assessment = AssessmentDAO.getAssessmentByORMID(id);
		if (assessment != null && assessment.getState() == Assessment.ACTIVE)
			return assessment;
		return null;
	}
	
	public static Assessment[] getTrashedAssessments() throws PersistentException {
		AssessmentCriteria criteria = new AssessmentCriteria();
		criteria.state.eq(Assessment.DELETED);
		return listAssessmentByCriteria(criteria);
	}
	
	
	public static Assessment getTrashedAssessment(int id) throws PersistentException {
		Assessment assessment = AssessmentDAO.getAssessmentByORMID(id);
		if (assessment != null && assessment.getState() == Assessment.DELETED)
			return assessment;
		return null;
	}
	
	public static Assessment[] getAssessmentsByCriteria(AssessmentCriteria criteria) {
		criteria.state.eq(Assessment.ACTIVE);
		return listAssessmentByCriteria(criteria);		
	}
	
	public static Assessment[] getTrashedAssessmentsByCriteria(AssessmentCriteria criteria) {
		criteria.state.eq(Assessment.DELETED);
		return listAssessmentByCriteria(criteria);		
	}
	
		
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	
	
	protected static Assessment loadAssessmentByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadAssessmentByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment getAssessmentByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getAssessmentByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment loadAssessmentByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadAssessmentByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment getAssessmentByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getAssessmentByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Assessment loadAssessmentByORMID(Session session, int id) throws PersistentException {
		try {
			return (Assessment) session.load(Assessment.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Assessment getAssessmentByORMID(Session session, int id) throws PersistentException {
		try {
			return (Assessment) session.get(Assessment.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Assessment loadAssessmentByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Assessment) session.load(Assessment.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Assessment getAssessmentByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Assessment) session.get(Assessment.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment[] listAssessmentByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listAssessmentByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment[] listAssessmentByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listAssessmentByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment[] listAssessmentByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Assessment as Assessment");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Assessment[]) list.toArray(new Assessment[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment[] listAssessmentByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Assessment as Assessment");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Assessment[]) list.toArray(new Assessment[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment loadAssessmentByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadAssessmentByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment loadAssessmentByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadAssessmentByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment loadAssessmentByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Assessment[] assessments = listAssessmentByQuery(session, condition, orderBy);
		if (assessments != null && assessments.length > 0)
			return assessments[0];
		else
			return null;
	}
	
	protected static Assessment loadAssessmentByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Assessment[] assessments = listAssessmentByQuery(session, condition, orderBy, lockMode);
		if (assessments != null && assessments.length > 0)
			return assessments[0];
		else
			return null;
	}
	
	protected static java.util.Iterator iterateAssessmentByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateAssessmentByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static java.util.Iterator iterateAssessmentByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateAssessmentByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static java.util.Iterator iterateAssessmentByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Assessment as Assessment");
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
	
	protected static java.util.Iterator iterateAssessmentByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Assessment as Assessment");
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
	
	protected static Assessment createAssessment() {
		return new Assessment();
	}
	
	public static boolean save(Assessment assessment) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(assessment);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Assessment assessment) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(assessment);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static boolean deleteAndDissociate(Assessment assessment)throws PersistentException {
		try {
			if(assessment.getAssessmentType() != null) {
				assessment.getAssessmentType().getAssessment().remove(assessment);
			}
			
			if(assessment.getTaxon() != null) {
				assessment.getTaxon().getAssessments().remove(assessment);
			}
			
			Edit[] lEdits = (Edit[])assessment.getEdit().toArray(new Edit[assessment.getEdit().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].getAssessment().remove(assessment);
			}
			Reference[] lReferences = (Reference[])assessment.getReference().toArray(new Reference[assessment.getReference().size()]);
			for(int i = 0; i < lReferences.length; i++) {
				lReferences[i].getAssessment().remove(assessment);
			}
			Field[] lFields = (Field[])assessment.getField().toArray(new Field[assessment.getField().size()]);
			for(int i = 0; i < lFields.length; i++) {
				lFields[i].setAssessment(null);
			}
			return delete(assessment);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static boolean deleteAndDissociate(Assessment assessment, Session session)throws PersistentException {
		try {
			if(assessment.getAssessmentType() != null) {
				assessment.getAssessmentType().getAssessment().remove(assessment);
			}
			
			if(assessment.getTaxon() != null) {
				assessment.getTaxon().getAssessments().remove(assessment);
			}
			
			Edit[] lEdits = (Edit[])assessment.getEdit().toArray(new Edit[assessment.getEdit().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].getAssessment().remove(assessment);
			}
			Reference[] lReferences = (Reference[])assessment.getReference().toArray(new Reference[assessment.getReference().size()]);
			for(int i = 0; i < lReferences.length; i++) {
				lReferences[i].getAssessment().remove(assessment);
			}
			Field[] lFields = (Field[])assessment.getField().toArray(new Field[assessment.getField().size()]);
			for(int i = 0; i < lFields.length; i++) {
				lFields[i].setAssessment(null);
			}
			try {
				session.delete(assessment);
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
	
	protected static boolean refresh(Assessment assessment) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(assessment);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static boolean evict(Assessment assessment) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(assessment);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment loadAssessmentByCriteria(AssessmentCriteria assessmentCriteria) {
		Assessment[] assessments = listAssessmentByCriteria(assessmentCriteria);
		if(assessments == null || assessments.length == 0) {
			return null;
		}
		return assessments[0];
	}
	
	protected static Assessment[] listAssessmentByCriteria(AssessmentCriteria assessmentCriteria) {
		return assessmentCriteria.listAssessment();
	}
}
