package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Caches notes.
 * 
 * @author adam.schwartz
 */
public class NotesCache {

	public static NotesCache impl = new NotesCache();

	private HashMap<String, HashMap<String, List<Notes>>> noteMap;

	private NotesCache() {
		noteMap = new HashMap<String, HashMap<String, List<Notes>>>();
	}

	public void addNote(final Field field, Notes note, final Assessment assessment, final GenericCallback<String> callback) {

		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, assessment)) {
			WindowUtils.errorAlert("You do not have sufficient permissions to perform " + "this operation.");
			return;
		}
		
		note.setField(field);
		
		/*String type = assessment.getType();
		String url = UriBase.getInstance().getNotesBase() + "/notes/" + type;
		url += "/" + assessment.getId();
		url += "/" + canonicalName;*/

		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.post(UriBase.getInstance().getNotesBase() + "/notes/field/" + field.getId(), note.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final Notes fullNote = Notes.fromXML(doc.getDocumentElement());
				
				addNoteToCache(field, fullNote, assessment);
				
				if (field.getNotes() == null)
					field.setNotes(new HashSet<Notes>());
				field.getNotes().add(fullNote);
				
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	private void addNoteToCache(final Field field, Notes currentNotes, Assessment assessment) {
		HashMap<String, List<Notes>> map = null;
		if (noteMap.containsKey(getNoteMapID(assessment)))
			map = noteMap.get(getNoteMapID(assessment));
		else
			map = new HashMap<String, List<Notes>>();
		
		final String cacheKey = field.getName() + ":" + field.getId();

		List<Notes> noteList = null;
		if (map.containsKey(cacheKey))
			noteList = map.get(cacheKey);
		else
			noteList = new ArrayList<Notes>();
		noteList.add(currentNotes);
		
		map.put(cacheKey, noteList);
		
		noteMap.put(getNoteMapID(assessment), map);
	}

	public void deleteNote(final Field field, final Notes currentNote, final Assessment assessment, final GenericCallback<String> callback) {
		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, assessment)) {
			WindowUtils.errorAlert("You do not have sufficient permissions to perform " + "this operation.");
			return;
		}

		String url = UriBase.getInstance().getNotesBase() + "/notes/note/" + currentNote.getId();

		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.delete(url, new GenericCallback<String>() {
			public void onSuccess(String result) {
				field.getNotes().remove(currentNote);
				
				removeNoteFromCache(field, currentNote, assessment);
				
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public void fetchNotes(final Assessment assessment, final GenericCallback<String> callback) {
		if (!noteMap.containsKey(getNoteMapID(assessment))) {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.get(UriBase.getInstance().getNotesBase() + "/notes/assessment/" + assessment.getId(),
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Debug.println("Failed to fetch notes for " + assessment.getId());
				}
				public void onSuccess(String result) {
					final HashMap<String, List<Notes>> assessNotes = new HashMap<String, List<Notes>>();
					
					final NativeNodeList fieldList = ndoc.getDocumentElement().getElementsByTagName("field");
					for (int i = 0; i < fieldList.getLength(); i++) {
						final NativeElement currentField = fieldList.elementAt(i);
						
						assessNotes.put(currentField.getAttribute("name"), Notes.notesFromXML(currentField));
					}
					
					noteMap.put(getNoteMapID(assessment), assessNotes);

					callback.onSuccess(result);
				}
			});
		} else
			callback.onSuccess("OK");
	}

	private String getNoteMapID(Assessment assessment) {
		return assessment.getId() + assessment.getType();
	}

	public HashMap<String, List<Notes>> getNotesForAssessment(Assessment assessment) {
		return noteMap.get(getNoteMapID(assessment));
	}

	public List<Notes> getNotesForCurrentAssessment(String fieldName) {
		Assessment cur = AssessmentCache.impl.getCurrentAssessment();
		if (cur == null)
			return null;

		HashMap<String, List<Notes>> notes = noteMap.get(getNoteMapID(cur));

		if (notes != null)
			return notes.get(fieldName);
		else
			return null;

	}

	private void removeNoteFromCache(final Field field, Notes currentNote, Assessment assessment) {
		final String cacheKey = field.getName() + ":" + field.getId();
		
		HashMap<String, List<Notes>> map = noteMap.get(getNoteMapID(assessment));
		
		List<Notes> noteList = map.get(cacheKey);

		noteList.remove(currentNote);
	}
}
