package org.iucn.sis.shared.taxonomyTree;

import java.util.ArrayList;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.assessments.Note;

public class CommonNameData implements Comparable<CommonNameData>, AuthorizableObject {
	public static final int UNSET = 0;
	public static final int ADDED = 1;
	public static final int DELETED = 2;
	public static final int ISO_CHANGED = 3;
	public static final int NAME_CHANGED = 4;

	public static final String[] reasons = { "", "ADDED", "DELETED", "ISO CHANGED", "NAME CHANGED" };

	private String name;
	private String language;
	private String isoCode;
	private boolean validated = false;
	private int changeReason;
	private boolean primary = false;

	private String assessmentAttachedToID;
	private String assessmentStatus;

	/**
	 * ArrayList<Reference>
	 */
	private ArrayList<ReferenceUI> sources;

	/**
	 * ArrayList<Note>
	 */
	private ArrayList<Note> notes;

	public CommonNameData(String name, String language, String isoCode) {
		this(name, language, isoCode, false, false);
	}

	public CommonNameData(String name, String language, String isoCode, boolean validated, boolean isPrimary) {
		super();
		this.name = name;
		this.language = language;
		this.isoCode = isoCode;
		this.validated = validated;
		this.primary = isPrimary;

		this.assessmentAttachedToID = "";
		this.assessmentStatus = "";

		sources = new ArrayList<ReferenceUI>();
		notes = new ArrayList<Note>();
	}

	public CommonNameData(String name, String language, String isoCode, boolean validated, int changeReason,
			boolean primary, String assessmentAttachedToID, String assessmentStatus, ArrayList<ReferenceUI> sources, ArrayList<Note> notes) {
		this.name = name;
		this.language = language;
		this.isoCode = isoCode;
		this.validated = validated;
		this.changeReason = changeReason;
		this.primary = primary;
		this.assessmentAttachedToID = assessmentAttachedToID;
		this.assessmentStatus = assessmentStatus;
		this.sources = sources;
		this.notes = notes;
	}

	public void addNote(Note note) {
		this.notes.add(note);
	}

	public void addSource(ReferenceUI source) {
		this.sources.add(source);
	}

	public String getFullURI() {
		return "resource/taxon/commonName";
	}
	
	public String getProperty(String key) {
		return "";
	}
	
	public int compareTo(CommonNameData other) {
		if (this.primary == true) {
			return -1;
		}

		if (name.compareTo(other.getName()) <= 0)
			return 0;
		return 1;
	}

	public CommonNameData deepCopy() {
		return new CommonNameData(name, language, isoCode, validated, changeReason, primary, assessmentAttachedToID,
				assessmentStatus, sources, notes);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CommonNameData) {
			CommonNameData other = (CommonNameData) o;
			if (other.getName().equalsIgnoreCase(name) && other.getLanguage().equalsIgnoreCase(language)
					&& other.getIsoCode().equalsIgnoreCase(isoCode))
				return true;
		}

		return false;
	}

	public String getAssessmentAttachedToID() {
		return assessmentAttachedToID;
	}

	public String getAssessmentStatus() {
		return assessmentStatus;
	}

	public int getChangeReason() {
		return changeReason;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public String getLanguage() {
		return language;
	}

	public String getName() {
		return name;
	}

	public ArrayList<Note> getNotes() {
		return notes;
	}

	public ArrayList<ReferenceUI> getSources() {
		return sources;
	}

	public boolean isPrimary() {
		return primary;
	}

	public boolean isValidated() {
		return validated;
	}

	public void removeNote(Note note) {
		this.notes.remove(note);
	}

	public boolean removeSource(ReferenceUI source) {
		return this.sources.remove(source);
	}

	public void setAssessmentAttachedToID(String assessmentAttachedToID) {
		this.assessmentAttachedToID = assessmentAttachedToID;
	}

	public void setAssessmentStatus(String assessmentStatus) {
		this.assessmentStatus = assessmentStatus;
	}

	public void setChangeReason(int reason) {
		validated = false;
		changeReason = reason;
	}

	public void setIsoCode(String isoCode) {
		validated = false;
		changeReason = ISO_CHANGED;
		this.isoCode = isoCode;
	}

	public void setLanguage(String language) {
		validated = false;
		changeReason = ISO_CHANGED;
		this.language = language;
	}

	public void setName(String name) {
		validated = false;
		changeReason = NAME_CHANGED;
		this.name = name;
	}

	public void setNotes(ArrayList<Note> notes) {
		this.notes = notes;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public void setSources(ArrayList<ReferenceUI> sources) {
		this.sources = sources;
	}

	public void setValidated(boolean validated, int changeReason) {
		this.validated = validated;
		this.changeReason = changeReason;
	}

	@Override
	public String toString() {
		return name;
	}
}
