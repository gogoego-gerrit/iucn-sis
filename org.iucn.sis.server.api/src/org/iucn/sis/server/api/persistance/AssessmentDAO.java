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

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentIntegrityValidation;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;

public class AssessmentDAO {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	
	public static Assessment getAssessment(Session session, int id) throws PersistentException {
		Assessment assessment = AssessmentDAO.getAssessmentByORMID(session, id);
		if (assessment != null && assessment.getState() == Assessment.ACTIVE)
			return assessment;
		return null;
	}
	
	public static Assessment[] getTrashedAssessments(Session session) throws PersistentException {
		try {
			AssessmentCriteria criteria = new AssessmentCriteria(session);
			criteria.state.eq(Assessment.DELETED);
			return listAssessmentByCriteria(criteria);
		} catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	
	public static Assessment getTrashedAssessment(Session session, int id) throws PersistentException {
		Assessment assessment = AssessmentDAO.getAssessmentByORMID(session, id);
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
	
	
	
	public static Assessment loadAssessmentByORMID(Session session, int id) throws PersistentException {
		try {
			return (Assessment) session.load(Assessment.class, new Integer(id));
		}
		catch (Exception e) {
			;
			throw new PersistentException(e);
		}
	}
	
	public static Assessment getAssessmentByORMID(Session session, int id) throws PersistentException {
		try {
			return (Assessment) session.get(Assessment.class, new Integer(id));
		}
		catch (Exception e) {
			;
			throw new PersistentException(e);
		}
	}
	
	public static Assessment loadAssessmentByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Assessment) session.load(Assessment.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			throw new PersistentException(e);
		}
	}
	
	public static Assessment getAssessmentByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Assessment) session.get(Assessment.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
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
			;
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
			;
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
			;
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
			;
			throw new PersistentException(e);
		}
	}
	
	protected static Assessment createAssessment() {
		return new Assessment();
	}
	
	/*public static boolean save(Assessment assessment) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, assessment);
			return true;
		}
		catch (Exception e) {
			;
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Assessment assessment) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, assessment);
			return true;
		}
		catch (Exception e) {
			;
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Assessment assessment) throws PersistentException {
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
				FieldDAO.deleteAndDissociate(lFields[i]);
			}
			return delete(assessment);
		}
		catch(Exception e) {
			;
			throw new PersistentException(e);
		}
	}*/
	
	public static boolean deleteAndDissociate(Assessment assessment, Session session)throws PersistentException {
		try {
			if(assessment.getAssessmentType() != null) {
				assessment.getAssessmentType().getAssessment().remove(assessment);
			}
			
			if(assessment.getTaxon() != null) {
				assessment.getTaxon().getAssessments().remove(assessment);
			}
			
			AssessmentIntegrityValidation validation = assessment.getValidation();
			if (validation != null) {
				assessment.setValidation(null);
				session.delete(validation);	
			}
			
			assessment.setPublicationReference(null);
			
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
			;
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
