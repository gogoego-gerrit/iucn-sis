package org.iucn.sis.server.api.io;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.server.api.persistance.NotesDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Assessment;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean delete(Notes note) {
		try {
			Taxon taxon = note.getCommonName().getTaxon();
			note.getCommonNames().remove(note);
			taxon.toXML();
			Integer id = note.getId();
			if (NotesDAO.deleteAndDissociate(note)) {
				updatedNoteToTaxon.put(id, note.getTaxon());
				return true;
			}
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	

}
