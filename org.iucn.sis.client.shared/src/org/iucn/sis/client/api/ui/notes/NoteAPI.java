package org.iucn.sis.client.api.ui.notes;

import java.util.Collection;

import org.iucn.sis.shared.api.models.Notes;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;

public interface NoteAPI {
	
	public void loadNotes(ComplexListener<Collection<Notes>> listener);
	
	public void addNote(Notes note, GenericCallback<Object> callback);
	
	public void deleteNote(Notes note, GenericCallback<Object> callback);
	
	public void onClose();

}
