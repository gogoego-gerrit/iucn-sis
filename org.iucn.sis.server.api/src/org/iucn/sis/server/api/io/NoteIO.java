package org.iucn.sis.server.api.io;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.NotesCriteria;
import org.iucn.sis.server.api.persistance.NotesDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Taxon;

public class NoteIO {
	
	/**
	 * FIXME: WTF?
	 * I get that this is so the listener will remove 
	 * the note from the taxon ... but why not just 
	 * save the taxon here?
	 */
	protected Map<Integer, Taxon> updatedNoteToTaxon;
	
	private final Session session;
	
	public NoteIO(Session session) {
		this.session = session;
		updatedNoteToTaxon = new HashMap<Integer, Taxon>();
	}
	
	protected void addNoteToTaxon(Notes note) {
		if ( note.getTaxon() != null) {
			updatedNoteToTaxon.put(note.getId(), note.getTaxon());
		}
	}
	
	public Notes[] getOfflineCreatedNotes() throws PersistentException {
		NotesCriteria criteria = new NotesCriteria(session);
		criteria.offlineStatus.eq(true);
		
		return NotesDAO.listNotesByCriteria(criteria);
	}
	
	public Taxon getNoteFromTaxon(Integer noteID) {
		return updatedNoteToTaxon.remove(noteID);
	}
		
	public Notes get(Integer noteID) {
		try {
			return NotesDAO.getNotesByORMID(session, noteID);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			Debug.println(e);
			return null;
		}
	}
	
	public boolean save(Notes note) {
		try {
			SIS.get().getManager().saveObject(session, note);
			addNoteToTaxon(note);
			return true;
		} catch (PersistentException e) {
			Debug.println(e);
		}
		return false;
	}
	
	public boolean delete(Notes note) {
		Taxon taxonToSave = null;
		if (note.getField() != null) {
			note.getField().getNotes().remove(note);
			try {
				SIS.get().getManager().saveObject(session, note.getField());
			} catch (PersistentException e) {
				return false;
			}
		}
		else if (note.getTaxon() != null) {
			note.getTaxon().getNotes().remove(note);
			taxonToSave = note.getTaxon();
		}
		else if (note.getCommonName() != null) {
			note.getCommonName().getNotes().remove(note);
			taxonToSave = note.getCommonName().getTaxon();
		}
		else if (note.getSynonym() != null) {
			note.getSynonym().getNotes().remove(note);
			taxonToSave = note.getSynonym().getTaxon();
		}
		else if (note.getEdit() != null)
			note.getEdit().getNotes().remove(note);
		
		if (taxonToSave != null) {
			try {
				taxonToSave.toXML();
				Integer id = note.getId();
				if (NotesDAO.deleteAndDissociate(note, session)) {
					updatedNoteToTaxon.put(id, note.getTaxon());
					return true;
				}
			} catch (PersistentException e) {
				Debug.println(e);
			}
		}
		else {
			try {
				if (NotesDAO.deleteAndDissociate(note, session))
					return true;
			} catch (PersistentException e) {
				Debug.println(e);
			}
		}
		return false;
	}
	
	

}
