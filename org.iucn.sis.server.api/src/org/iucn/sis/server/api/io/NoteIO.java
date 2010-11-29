package org.iucn.sis.server.api.io;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.NotesDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Taxon;

public class NoteIO {
	
	protected Map<Integer, Taxon> updatedNoteToTaxon;
	
	public NoteIO() {
		updatedNoteToTaxon = new HashMap<Integer, Taxon>();
	}
	
	protected void addNoteToTaxon(Notes note) {
		if ( note.getTaxon() != null) {
			updatedNoteToTaxon.put(note.getId(), note.getTaxon());
		}
	}
	
	public Taxon getNoteFromTaxon(Integer noteID) {
		return updatedNoteToTaxon.remove(noteID);
	}
		
	public Notes get(Integer noteID) {
		try {
			return NotesDAO.getNotesByORMID(noteID);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean save(Notes note) {
		try {
			if (NotesDAO.save(note)) {
				addNoteToTaxon(note);
				return true;
			}			
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
				FieldDAO.save(note.getField());
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
				if (NotesDAO.deleteAndDissociate(note)) {
					updatedNoteToTaxon.put(id, note.getTaxon());
					return true;
				}
			} catch (PersistentException e) {
				Debug.println(e);
			}
		}
		else {
			try {
				if (NotesDAO.deleteAndDissociate(note))
					return true;
			} catch (PersistentException e) {
				Debug.println(e);
			}
		}
		return false;
	}
	
	

}
