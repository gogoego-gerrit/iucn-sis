package org.iucn.sis.shared.data.assessments;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.acl.base.AuthorizableObject;

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

	private HashMap<String, HashMap<String, ArrayList<Note>>> noteMap;

	private NotesCache() {
		noteMap = new HashMap<String, HashMap<String, ArrayList<Note>>>();
	}

	public void addNote(final String canonicalName, Note currentNote, AssessmentData assessment,
			GenericCallback<String> callback) {

		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, assessment)) {
			WindowUtils.errorAlert("You do not have sufficient permissions to perform " + "this operation.");
			return;
		}

		NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		String type = assessment.getType();
		String url = "/notes/" + type;
		url += "/" + assessment.getAssessmentID();
		url += "/" + canonicalName;

		if (type.equals(BaseAssessment.USER_ASSESSMENT_STATUS))
			url += "/" + currentNote.getUser();

		addNoteToCache(canonicalName, currentNote, assessment);

		doc.post(url, currentNote.toXML(), callback);
	}

	private void addNoteToCache(final String cName, Note currentNote, AssessmentData assessment) {
		ArrayList<Note> noteList = null;
		HashMap<String, ArrayList<Note>> assMap = null;

		if (noteMap.containsKey(getNoteMapID(assessment)))
			assMap = noteMap.get(getNoteMapID(assessment));
		else
			assMap = new HashMap<String, ArrayList<Note>>();

		if (assMap.containsKey(cName))
			noteList = assMap.get(cName);
		else
			noteList = new ArrayList<Note>();

		noteList.add(currentNote);
		assMap.put(cName, noteList);
		noteMap.put(getNoteMapID(assessment), assMap);
	}

	public void deleteNote(final String canonicalName, Note currentNote, AssessmentData assessment,
			GenericCallback<String> callback) {

		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, assessment)) {
			WindowUtils.errorAlert("You do not have sufficient permissions to perform " + "this operation.");
			return;
		}

		NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		String type = AssessmentCache.impl.getCurrentAssessment().getType();
		String url = "/notes/" + type;
		url += "/" + AssessmentCache.impl.getCurrentAssessment().getAssessmentID();
		url += "/" + canonicalName;

		if (type.equals(BaseAssessment.USER_ASSESSMENT_STATUS))
			url += "/" + currentNote.getUser();

		removeNoteFromCache(canonicalName, currentNote, assessment);

		doc.post(url + "?option=remove", currentNote.toXML(), callback);
	}

	public void fetchNotes(final AssessmentData assessment, final GenericCallback<String> callback) {
		if (!noteMap.containsKey(getNoteMapID(assessment))) {
			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.get("/notes/" + assessment.getType() + "/" + assessment.getAssessmentID(),
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							callback.onSuccess("OK");
						}

						public void onSuccess(String result) {
							HashMap<String, ArrayList<Note>> assessNotes = new HashMap<String, ArrayList<Note>>();
							NativeNodeList nodeList = ndoc.getDocumentElement().getElementsByTagName("notes");

							for (int i = 0; i < nodeList.getLength(); i++) {
								NativeElement el = nodeList.elementAt(i);
								String canonicalName = el.getAttribute("id");
								assessNotes.put(canonicalName, Note.notesFromXML(el));
							}

							noteMap.put(getNoteMapID(assessment), assessNotes);

							callback.onSuccess(result);
						}
					});
		} else
			callback.onSuccess("OK");
	}

	private String getNoteMapID(AssessmentData assessment) {
		return assessment.getAssessmentID() + assessment.getType();
	}

	public HashMap<String, ArrayList<Note>> getNotesForAssessment(AssessmentData assessment) {
		return noteMap.get(getNoteMapID(assessment));
	}

	public ArrayList<Note> getNotesForCurrentAssessment(String fieldName) {
		AssessmentData cur = AssessmentCache.impl.getCurrentAssessment();
		if (cur == null)
			return null;

		HashMap<String, ArrayList<Note>> notes = noteMap.get(getNoteMapID(cur));

		if (notes != null)
			return notes.get(fieldName);
		else
			return null;

	}

	private void removeNoteFromCache(final String cName, Note currentNote, AssessmentData assessment) {

		HashMap<String, ArrayList<Note>> assMap = noteMap.get(getNoteMapID(assessment));
		ArrayList<Note> noteList = assMap.get(cName);

		noteList.remove(currentNote);
	}
}
