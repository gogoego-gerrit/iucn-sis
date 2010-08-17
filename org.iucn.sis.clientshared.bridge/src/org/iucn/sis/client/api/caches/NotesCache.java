package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.Assessment;
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

	public void addNote(final String canonicalName, Notes currentNote, Assessment assessment,
			GenericCallback<String> callback) {

		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, assessment)) {
			WindowUtils.errorAlert("You do not have sufficient permissions to perform " + "this operation.");
			return;
		}

		NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		String type = assessment.getType();
		String url = UriBase.getInstance().getNotesBase() + "/notes/" + type;
		url += "/" + assessment.getId();
		url += "/" + canonicalName;

		addNoteToCache(canonicalName, currentNote, assessment);

		doc.post(url, currentNote.toXML(), callback);
	}

	private void addNoteToCache(final String cName, Notes currentNotes, Assessment assessment) {
		List<Notes> noteList = null;
		HashMap<String, List<Notes>> assMap = null;

		if (noteMap.containsKey(getNoteMapID(assessment)))
			assMap = noteMap.get(getNoteMapID(assessment));
		else
			assMap = new HashMap<String, List<Notes>>();

		if (assMap.containsKey(cName))
			noteList = assMap.get(cName);
		else
			noteList = new ArrayList<Notes>();

		noteList.add(currentNotes);
		assMap.put(cName, noteList);
		noteMap.put(getNoteMapID(assessment), assMap);
	}

	public void deleteNote(final String canonicalName, Notes currentNote, Assessment assessment,
			GenericCallback<String> callback) {

		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, assessment)) {
			WindowUtils.errorAlert("You do not have sufficient permissions to perform " + "this operation.");
			return;
		}

		NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		String type = AssessmentCache.impl.getCurrentAssessment().getType();
		String url = UriBase.getInstance().getNotesBase() + "/notes/" + type;
		url += "/" + AssessmentCache.impl.getCurrentAssessment().getId();
		url += "/" + canonicalName;

		removeNoteFromCache(canonicalName, currentNote, assessment);

		doc.post(url + "?option=remove", currentNote.toXML(), callback);
	}

	public void fetchNotes(final Assessment assessment, final GenericCallback<String> callback) {
		if (!noteMap.containsKey(getNoteMapID(assessment))) {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.get(UriBase.getInstance().getNotesBase() + "/notes/" + assessment.getType() + "/" + assessment.getId(),
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							callback.onSuccess("OK");
						}

						public void onSuccess(String result) {
							HashMap<String, List<Notes>> assessNotes = new HashMap<String, List<Notes>>();
							NativeNodeList nodeList = ndoc.getDocumentElement().getElementsByTagName("notes");

							for (int i = 0; i < nodeList.getLength(); i++) {
								NativeElement el = nodeList.elementAt(i);
								String canonicalName = el.getAttribute("id");
								assessNotes.put(canonicalName, Notes.notesFromXML(el));
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

	private void removeNoteFromCache(final String cName, Notes currentNote, Assessment assessment) {

		HashMap<String, List<Notes>> assMap = noteMap.get(getNoteMapID(assessment));
		List<Notes> noteList = assMap.get(cName);

		noteList.remove(currentNote);
	}
}
