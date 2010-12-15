package org.iucn.sis.client.panels.dem;

import java.util.ArrayList;
import java.util.Collection;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.ui.notes.NoteAPI;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Notes;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

public class DEMToolsPopups {
	

	public static void buildNotePopup() {
		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();

		if (assessment == null)
			return;

		NotesWindow window = new NotesWindow(new AssessmentNotes(assessment));
		window.setHeading("Notes for " + assessment.getSpeciesName());
		window.show();
	}
	
	private static class AssessmentNotes implements NoteAPI {
		
		private final Assessment assessment;
		
		public AssessmentNotes(Assessment assessment) {
			this.assessment = assessment;
		}
		
		@Override
		public void addNote(Notes note, GenericCallback<Object> callback) {
			WindowUtils.errorAlert("Adding notes is not supported at this level.");
		}
		
		public void deleteNote(Notes note, com.solertium.lwxml.shared.GenericCallback<Object> callback) {
			WindowUtils.errorAlert("Removing notes is not supported at this level.");
		}
		
		@Override
		public void loadNotes(final ComplexListener<Collection<Notes>> listener) {
			String url = UriBase.getInstance().getNotesBase() + "/notes/assessment/" + assessment.getId();
			/*if (type.equals(AssessmentType.USER_ASSESSMENT_TYPE))
				url += "/" + SimpleSISClient.currentUser.getUsername();*/
			
			final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			doc.get(url, new GenericCallback<String>() {
				public void onSuccess(String result) {
					listener.handleEvent(Notes.notesFromXML(doc.getDocumentElement()));
				}
				public void onFailure(Throwable caught) {
					listener.handleEvent(new ArrayList<Notes>());
				}
			});
		}
		
		@Override
		public void onClose() {
			// TODO Auto-generated method stub
			
		}
		
	}

	
}
