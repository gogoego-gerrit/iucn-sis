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
import org.iucn.sis.shared.api.models.Edit;

public class EditDAO {
	public static Edit loadEditByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadEditByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit getEditByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getEditByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit loadEditByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadEditByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit getEditByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getEditByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit loadEditByORMID(Session session, int id) throws PersistentException {
		try {
			return (Edit) session.load(Edit.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit getEditByORMID(Session session, int id) throws PersistentException {
		try {
			return (Edit) session.get(Edit.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit loadEditByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Edit) session.load(Edit.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit getEditByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Edit) session.get(Edit.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit[] listEditByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listEditByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit[] listEditByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listEditByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit[] listEditByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Edit as Edit");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Edit[]) list.toArray(new Edit[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit[] listEditByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Edit as Edit");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Edit[]) list.toArray(new Edit[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit loadEditByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadEditByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit loadEditByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadEditByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit loadEditByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Edit[] edits = listEditByQuery(session, condition, orderBy);
		if (edits != null && edits.length > 0)
			return edits[0];
		else
			return null;
	}
	
	public static Edit loadEditByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Edit[] edits = listEditByQuery(session, condition, orderBy, lockMode);
		if (edits != null && edits.length > 0)
			return edits[0];
		else
			return null;
	}
	
	public static java.util.Iterator iterateEditByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateEditByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateEditByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateEditByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateEditByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Edit as Edit");
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
	
	public static java.util.Iterator iterateEditByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Edit as Edit");
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
	
	public static Edit createEdit() {
		return new Edit();
	}
	
	public static boolean save(Edit edit) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(edit);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Edit edit) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(edit);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	/**
	public static boolean deleteAndDissociate(Edit edit)throws PersistentException {
		try {
			if(edit.getUser() != null) {
				edit.getUser().getEdit().remove(edit);
			}
			
			WorkingSet[] lWorking_sets = (WorkingSet[])edit.getWorking_set().toArray(new WorkingSet[edit.getWorking_set().size()]);
			for(int i = 0; i < lWorking_sets.length; i++) {
				lWorking_sets[i].getEdit().remove(edit);
			}
			Assessment[] lAssessments = (Assessment[])edit.getAssessment().toArray(new Assessment[edit.getAssessment().size()]);
			for(int i = 0; i < lAssessments.length; i++) {
				lAssessments[i].getEdit().remove(edit);
			}
			Taxon[] lTaxons = (Taxon[])edit.getTaxon().toArray(new Taxon[edit.getTaxon().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getEdits().remove(edit);
			}
			Notes[] lNotess = (Notes[])edit.getNotes().toArray(new Notes[edit.getNotes().size()]);
			for(int i = 0; i < lNotess.length; i++) {
				lNotess[i].getEdit().remove(edit);
			}
			return delete(edit);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean deleteAndDissociate(Edit edit, org.orm.Session session)throws PersistentException {
		try {
			if(edit.getUser() != null) {
				edit.getUser().getEdit().remove(edit);
			}
			
			WorkingSet[] lWorking_sets = (WorkingSet[])edit.getWorking_set().toArray(new WorkingSet[edit.getWorking_set().size()]);
			for(int i = 0; i < lWorking_sets.length; i++) {
				lWorking_sets[i].getEdit().remove(edit);
			}
			Assessment[] lAssessments = (Assessment[])edit.getAssessment().toArray(new Assessment[edit.getAssessment().size()]);
			for(int i = 0; i < lAssessments.length; i++) {
				lAssessments[i].getEdit().remove(edit);
			}
			Taxon[] lTaxons = (Taxon[])edit.getTaxon().toArray(new Taxon[edit.getTaxon().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getEdits().remove(edit);
			}
			Notes[] lNotess = (Notes[])edit.getNotes().toArray(new Notes[edit.getNotes().size()]);
			for(int i = 0; i < lNotess.length; i++) {
				lNotess[i].getEdit().remove(edit);
			}
			try {
				session.delete(edit);
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
	
	public static boolean refresh(Edit edit) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(edit);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Edit edit) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(edit);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Edit loadEditByCriteria(EditCriteria editCriteria) {
		Edit[] edits = listEditByCriteria(editCriteria);
		if(edits == null || edits.length == 0) {
			return null;
		}
		return edits[0];
	}
	
	public static Edit[] listEditByCriteria(EditCriteria editCriteria) {
		return editCriteria.listEdit();
	}
}
