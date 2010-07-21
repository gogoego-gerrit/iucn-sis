package org.iucn.sis.shared.api.models;

/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class Notes implements Serializable {

	public static String ROOT_TAG = "note";

	public static Notes fromXML(NativeElement current) {
		Notes note = new Notes();
		note.setValue(current.getElementsByTagName("value").item(0).getTextContent());
		note.setId(Integer.valueOf(current.getAttribute("id")));
		
		NativeNodeList edits = current.getElementsByTagName(Edit.ROOT_TAG);
		for (int i = 0; i < edits.getLength(); i++) {
			note.getEdits().add(Edit.fromXML(edits.elementAt(i)));
		}
		
		return note;
	}

	public static List<Notes> notesFromXML(NativeElement element) {
		List<Notes> notes = new ArrayList<Notes>();
		NativeNodeList list = element.getElementsByTagName(Notes.ROOT_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			notes.add(Notes.fromXML(list.elementAt(i)));
		}
		return notes;
	}

	private int id;
	private String value;
	private Set<Synonym> synonyms;
	private Set<CommonName> commonNames;
	private Set<Taxon> taxa;
	private Set<Edit> edits;
	private Set<Field> fields;
	
	
	public Notes() {
		synonyms = new HashSet<Synonym>();
		commonNames = new HashSet<CommonName>();
		taxa = new HashSet<Taxon>();
		edits = new HashSet<Edit>();
		fields = new HashSet<Field>();
	}
	public Notes deepCopy() {
		Notes note = new Notes();
		note.setValue(getValue());
		return note;
	}
	public CommonName getCommonName() {
		return commonNames.iterator().hasNext() ? commonNames.iterator().next() : null;
	}

	public Set<CommonName> getCommonNames() {
		return commonNames;
	}

	public Edit getEdit() {
		return edits.iterator().hasNext() ? edits.iterator().next() : null;
	}

	public Set<Edit> getEdits() {
		return edits;
	}

	public Field getField() {
		return fields.iterator().hasNext() ? fields.iterator().next() : null;
	}

	public Set<Field> getFields() {
		return fields;
	}

	public int getId() {
		return id;
	}

	public int getORMID() {
		return getId();
	}

	public Synonym getSynonym() {
		return synonyms.iterator().hasNext() ? synonyms.iterator().next() : null;
	}

	public Set<Synonym> getSynonyms() {
		return synonyms;
	}

	public Set<Taxon> getTaxa() {
		return taxa;
	}

	public Taxon getTaxon() {
		return taxa.iterator().hasNext() ? taxa.iterator().next() : null;
	}

	public String getValue() {
		return value;
	}

	public void setCommonName(CommonName value) {
		commonNames.clear();
		commonNames.add(value);
	}

	public void setCommonNames(Set<CommonName> commonNames) {
		this.commonNames = commonNames;
	}

	public void setEdit(Edit value) {
		edits.clear();
		edits.add(value);
	}

	public void setEdits(Set<Edit> edits) {
		this.edits = edits;
	}

	public void setField(Field value) {
		this.fields.clear();
		fields.add(value);
	}

	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}

	private void setId(int value) {
		this.id = value;
	}

	public void setSynonym(Synonym value) {
		synonyms.clear();
		synonyms.add(value);
	}

	public void setSynonyms(Set<Synonym> synonyms) {
		this.synonyms = synonyms;
	}

	public void setTaxa(Set<Taxon> taxa) {
		this.taxa = taxa;
	}

	public void setTaxon(Taxon value) {
		taxa.clear();
		taxa.add(value);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return String.valueOf(getId());
	}

	public String toXML() {
		StringBuilder xml =  new StringBuilder("<" + ROOT_TAG + " id=\"" + getId() + "\"><value><![CDATA[" + getValue() + "]]></value>");
		for (Edit edit : getEdits())
			xml.append(edit.toXML());
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();
	}

}
