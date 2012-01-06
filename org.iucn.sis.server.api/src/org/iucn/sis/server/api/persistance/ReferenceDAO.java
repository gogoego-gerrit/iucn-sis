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
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;

public class ReferenceDAO {
	/*public static Reference loadReferenceByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadReferenceByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Reference getReferenceByORMID(int id) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getReferenceByORMID(session, id);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Reference loadReferenceByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadReferenceByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Reference getReferenceByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return getReferenceByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Reference loadReferenceByORMID(Session session, int id) throws PersistentException {
		try {
			return (Reference) session.load(Reference.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static Reference getReferenceByORMID(Session session, int id) throws PersistentException {
		try {
			return (Reference) session.get(Reference.class, new Integer(id));
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static Reference loadReferenceByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Reference) session.load(Reference.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static Reference getReferenceByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Reference) session.get(Reference.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Reference[] listReferenceByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listReferenceByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Reference[] listReferenceByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return listReferenceByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	@SuppressWarnings("unchecked")
	public static Reference[] listReferenceByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Reference as Reference");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Reference[]) list.toArray(new Reference[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Reference[] listReferenceByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Reference as Reference");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Reference[]) list.toArray(new Reference[list.size()]);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	/*public static Reference loadReferenceByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadReferenceByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static Reference loadReferenceByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return loadReferenceByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	public static Reference loadReferenceByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Reference[] references = listReferenceByQuery(session, condition, orderBy);
		if (references != null && references.length > 0)
			return references[0];
		else
			return null;
	}
	
	public static Reference loadReferenceByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Reference[] references = listReferenceByQuery(session, condition, orderBy, lockMode);
		if (references != null && references.length > 0)
			return references[0];
		else
			return null;
	}
	
	/*public static java.util.Iterator iterateReferenceByQuery(String condition, String orderBy) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateReferenceByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}
	
	public static java.util.Iterator iterateReferenceByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Session session = SISPersistentManager.instance().openSession();
		try {
			return iterateReferenceByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		} finally {
			session.close();
		}
	}*/
	
	@SuppressWarnings("unchecked")
	public static java.util.Iterator iterateReferenceByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Reference as Reference");
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
	
	@SuppressWarnings("unchecked")
	public static java.util.Iterator iterateReferenceByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Reference as Reference");
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
	
	public static Reference createReference() {
		return new Reference();
	}
	
	/*public static boolean save(Reference reference) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(session, reference);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Reference reference) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(session, reference);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Reference reference)throws PersistentException {
		try {
			Synonym[] lSynonyms = (Synonym[])reference.getSynonym().toArray(new Synonym[reference.getSynonym().size()]);
			for(int i = 0; i < lSynonyms.length; i++) {
				lSynonyms[i].getReference().remove(reference);
			}
			CommonName[] lCommon_names = (CommonName[])reference.getCommon_name().toArray(new CommonName[reference.getCommon_name().size()]);
			for(int i = 0; i < lCommon_names.length; i++) {
				lCommon_names[i].getReference().remove(reference);
			}
			Assessment[] lAssessments = (Assessment[])reference.getAssessment().toArray(new Assessment[reference.getAssessment().size()]);
			for(int i = 0; i < lAssessments.length; i++) {
				lAssessments[i].getReference().remove(reference);
			}
			Field[] lFields = (Field[])reference.getField().toArray(new Field[reference.getField().size()]);
			for(int i = 0; i < lFields.length; i++) {
				lFields[i].getReference().remove(reference);
			}
			Taxon[] lTaxons = (Taxon[])reference.getTaxon().toArray(new Taxon[reference.getTaxon().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getReference().remove(reference);
			}
			return delete(reference);
		}
		catch(Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static boolean deleteAndDissociate(Reference reference, Session session)throws PersistentException {
		try {
			Synonym[] lSynonyms = (Synonym[])reference.getSynonym().toArray(new Synonym[reference.getSynonym().size()]);
			for(int i = 0; i < lSynonyms.length; i++) {
				lSynonyms[i].getReference().remove(reference);
			}
			CommonName[] lCommon_names = (CommonName[])reference.getCommon_name().toArray(new CommonName[reference.getCommon_name().size()]);
			for(int i = 0; i < lCommon_names.length; i++) {
				lCommon_names[i].getReference().remove(reference);
			}
			Assessment[] lAssessments = (Assessment[])reference.getAssessment().toArray(new Assessment[reference.getAssessment().size()]);
			for(int i = 0; i < lAssessments.length; i++) {
				lAssessments[i].getReference().remove(reference);
			}
			Field[] lFields = (Field[])reference.getField().toArray(new Field[reference.getField().size()]);
			for(int i = 0; i < lFields.length; i++) {
				lFields[i].getReference().remove(reference);
			}
			Taxon[] lTaxons = (Taxon[])reference.getTaxon().toArray(new Taxon[reference.getTaxon().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getReference().remove(reference);
			}
			try {
				session.delete(reference);
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
	
	/*public static boolean refresh(Reference reference) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(reference);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Reference reference) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(reference);
			return true;
		}
		catch (Exception e) {
			Debug.println(e);
			throw new PersistentException(e);
		}
	}*/
	
	public static Reference loadReferenceByCriteria(ReferenceCriteria referenceCriteria) {
		Reference[] references = listReferenceByCriteria(referenceCriteria);
		if(references == null || references.length == 0) {
			return null;
		}
		return references[0];
	}
	
	public static Reference[] listReferenceByCriteria(ReferenceCriteria referenceCriteria) {
		return referenceCriteria.listReference();
	}
}
