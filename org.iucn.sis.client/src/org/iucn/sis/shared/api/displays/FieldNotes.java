package org.iucn.sis.shared.api.displays;

import java.util.Collection;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.NotesCache;
import org.iucn.sis.client.panels.notes.NoteAPI;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Field;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;

public abstract class FieldNotes implements NoteAPI {
	
	private final Field field;
	
	public FieldNotes(Field field) {
		this.field = field;
	}

	public void addNote(Notes note, final GenericCallback<Object> callback) {
		NotesCache.impl.addNote(field, note, AssessmentCache.impl.getCurrentAssessment(),
				new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(String result) {
				callback.onSuccess(result);
			}
		});
	}
	
	public void deleteNote(Notes note, final GenericCallback<Object> callback) {
		NotesCache.impl.deleteNote(field, note, AssessmentCache.impl.getCurrentAssessment(),
				new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(String result) {
				callback.onSuccess(result);
			};
		});
	}
	
	public void loadNotes(ComplexListener<Collection<Notes>> listener) {
		listener.handleEvent(NotesCache.impl.getNotesForCurrentAssessment(field));
	}
	
	public void onClose() {
		List<Notes> list = NotesCache.impl.getNotesForCurrentAssessment(field);
		onClose(list);
	}
	
	public abstract void onClose(List<Notes> list);

}
