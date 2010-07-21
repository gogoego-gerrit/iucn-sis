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
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;

public class NotesDAO {
	public static Notes loadNotesByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadNotesByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes getNotesByORMID(int id) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getNotesByORMID(session, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes loadNotesByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadNotesByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes getNotesByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return getNotesByORMID(session, id, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes loadNotesByORMID(Session session, int id) throws PersistentException {
		try {
			return (Notes) session.load(Notes.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes getNotesByORMID(Session session, int id) throws PersistentException {
		try {
			return (Notes) session.get(Notes.class, new Integer(id));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes loadNotesByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Notes) session.load(Notes.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes getNotesByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			return (Notes) session.get(Notes.class, new Integer(id), lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes[] listNotesByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listNotesByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes[] listNotesByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return listNotesByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes[] listNotesByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Notes as Notes");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			List list = query.list();
			return (Notes[]) list.toArray(new Notes[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes[] listNotesByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Notes as Notes");
		if (condition != null)
			sb.append(" Where ").append(condition);
		if (orderBy != null)
			sb.append(" Order By ").append(orderBy);
		try {
			Query query = session.createQuery(sb.toString());
			query.setLockMode("this", lockMode);
			List list = query.list();
			return (Notes[]) list.toArray(new Notes[list.size()]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes loadNotesByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadNotesByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes loadNotesByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return loadNotesByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes loadNotesByQuery(Session session, String condition, String orderBy) throws PersistentException {
		Notes[] noteses = listNotesByQuery(session, condition, orderBy);
		if (noteses != null && noteses.length > 0)
			return noteses[0];
		else
			return null;
	}
	
	public static Notes loadNotesByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		Notes[] noteses = listNotesByQuery(session, condition, orderBy, lockMode);
		if (noteses != null && noteses.length > 0)
			return noteses[0];
		else
			return null;
	}
	
	public static java.util.Iterator iterateNotesByQuery(String condition, String orderBy) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateNotesByQuery(session, condition, orderBy);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateNotesByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		try {
			Session session = SISPersistentManager.instance().getSession();
			return iterateNotesByQuery(session, condition, orderBy, lockMode);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static java.util.Iterator iterateNotesByQuery(Session session, String condition, String orderBy) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Notes as Notes");
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
	
	public static java.util.Iterator iterateNotesByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
		StringBuffer sb = new StringBuffer("From Notes as Notes");
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
	
	public static Notes createNotes() {
		return new Notes();
	}
	
	public static boolean save(Notes notes) throws PersistentException {
		try {
			SISPersistentManager.instance().saveObject(notes);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean delete(Notes notes) throws PersistentException {
		try {
			SISPersistentManager.instance().deleteObject(notes);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	
	public static boolean deleteAndDissociate(Notes notes) throws PersistentException {
		try {
			Synonym[] lSynonyms = (Synonym[])notes.getSynonyms().toArray(new Synonym[notes.getSynonyms().size()]);
			for(int i = 0; i < lSynonyms.length; i++) {
				lSynonyms[i].getNotes().remove(notes);
			}
			
			Taxon[] lTaxons = (Taxon[])notes.getTaxa().toArray(new Taxon[notes.getTaxa().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getNotes().remove(notes);
			}
			Edit[] lEdits = (Edit[])notes.getEdits().toArray(new Edit[notes.getEdits().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].getNotes().remove(notes);
			}
			Field[] lFields = (Field[])notes.getFields().toArray(new Field[notes.getFields().size()]);
			for(int i = 0; i < lFields.length; i++) {
				lFields[i].getNotes().remove(notes);
			}
			return delete(notes);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	/**
	
	public static boolean deleteAndDissociate(Notes notes, org.orm.Session session)throws PersistentException {
		try {
			Synonym[] lSynonyms = (Synonym[])notes.getSynonym().toArray(new Synonym[notes.getSynonym().size()]);
			for(int i = 0; i < lSynonyms.length; i++) {
				lSynonyms[i].getNotes().remove(notes);
			}
			CommonName[] lCommon_names = (CommonName[])notes.getCommon_name().toArray(new CommonName[notes.getCommon_name().size()]);
			for(int i = 0; i < lCommon_names.length; i++) {
				lCommon_names[i].getNotes().remove(notes);
			}
			Taxon[] lTaxons = (Taxon[])notes.getTaxon().toArray(new Taxon[notes.getTaxon().size()]);
			for(int i = 0; i < lTaxons.length; i++) {
				lTaxons[i].getNotes().remove(notes);
			}
			Edit[] lEdits = (Edit[])notes.getEdit().toArray(new Edit[notes.getEdit().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].getNotes().remove(notes);
			}
			Field[] lFields = (Field[])notes.getField().toArray(new Field[notes.getField().size()]);
			for(int i = 0; i < lFields.length; i++) {
				lFields[i].getNotes().remove(notes);
			}
			try {
				session.delete(notes);
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
	
	public static boolean refresh(Notes notes) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().refresh(notes);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static boolean evict(Notes notes) throws PersistentException {
		try {
			SISPersistentManager.instance().getSession().evict(notes);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PersistentException(e);
		}
	}
	
	public static Notes loadNotesByCriteria(NotesCriteria notesCriteria) {
		Notes[] noteses = listNotesByCriteria(notesCriteria);
		if(noteses == null || noteses.length == 0) {
			return null;
		}
		return noteses[0];
	}
	
	public static Notes[] listNotesByCriteria(NotesCriteria notesCriteria) {
		return notesCriteria.listNotes();
	}
}
