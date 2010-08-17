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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class Field implements Serializable {

	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	public static final String ROOT_TAG = "field";

	public static Field fromXML(NativeElement element) {
		Field field = new Field();
		String name = element.getAttribute("name");
		field.setName(name);
		String id = element.getAttribute("id");
		try {
		field.setId(Integer.valueOf(id).intValue());
		} catch (NumberFormatException e) {
			System.out.println("ERROR - FIELD " + name + " DOES NOT HAVE AN ID!!! trying to parse " + id + " with name " + name);
		}
		
		field.setFields(new HashSet<Field>());
		NativeNodeList subfields = element.getElementsByTagName("subfields");
		if (subfields.getLength() > 0) {
			NativeNodeList subsubFields = subfields.elementAt(0).getElementsByTagName(Field.ROOT_TAG);
			for( int i = 0; i < subsubFields.getLength(); i++ ) {
				Field cur = Field.fromXML(subsubFields.elementAt(i));
				cur.setParent(field);
				field.getFields().add(cur);
			}
		}
		
		
		field.setPrimitiveField(new HashSet<PrimitiveField>());
		NativeNodeList prims = element.getElementsByTagName(PrimitiveField.ROOT_TAG);
		for( int i = 0; i < prims.getLength(); i++ ) {
			NativeElement primEl = prims.elementAt(i);
			PrimitiveField cur = PrimitiveFieldFactory.generatePrimitiveField(primEl.getAttribute(PrimitiveField.TYPE_TAG));
			cur.fromXML(primEl);
			cur.setField(field);
			field.getPrimitiveField().add(cur);
		}
		
		field.setNotes(new HashSet<Notes>());
		NativeNodeList notes = element.getElementsByTagName(Notes.ROOT_TAG);
		for( int i = 0; i < notes.getLength(); i++ ) {
			Notes cur = Notes.fromXML(notes.elementAt(i));
			cur.setField(field);
			field.getNotes().add(cur);
		}
		
		field.setReference(new HashSet<Reference>());
		NativeNodeList refs = element.getElementsByTagName(Reference.ROOT_TAG);
		for( int i = 0; i < refs.getLength(); i++ ) {
			Reference cur = Reference.fromXML(refs.elementAt(i));
			if( cur.getField() == null )
				cur.setField(new HashSet<Field>());
			
			cur.getField().add(field);
			field.getReference().add(cur);
		}
		
		return field;
	}

	private int id;

	private Assessment assessment;

	private String name;
	
	private Field parent;
	
	private java.util.Set<Notes> notes;	
	
	private java.util.Set<Field> fields;
	
	private java.util.Set<Reference> reference;

	private java.util.Set<PrimitiveField> primitiveField;
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */

	public Field() {
		notes = new java.util.HashSet<Notes>();
		fields = new java.util.HashSet<Field>();
		reference = new java.util.HashSet<Reference>();
		primitiveField = new java.util.HashSet<PrimitiveField>();
	}

	public Field(String canonicalName, Assessment assessment) {
		this();
		this.name = canonicalName;
		this.assessment = assessment;
	}

	/**
	 * 
	 *  Copies all information from this field into the 
	 * parameter field.  
	 * 
	 * @param field -- all information is copied into this
	 * @param append -- if information should be appended if field is a narrative field
	 * @param overwrite -- if data is not null, should it be overwritten
	 * @return boolean if data has been changed in field
	 */
	public boolean copyInto(Field field, boolean append, boolean overwrite) {
		
		//QUIT IF THE FIELDS ARE EQUAL IF DATA IS THERE AND WE ARE NOT APPENDING AND NOT OVERWRITING
		if (this.equals(field) || (field != null && !overwrite && !field.isNarrativeField()) || (field != null && field.isNarrativeField() && (!append || !overwrite)))
			return false;
			
		field.setName(getName());
		if (overwrite) {
			field.setFields(new HashSet<Field>());
			field.setPrimitiveField(new HashSet<PrimitiveField>());
		}
		
		Map<String,PrimitiveField> keyToPF = field.getKeyToPrimitiveFields();
		for (PrimitiveField pf : getPrimitiveField()) {			
			if (keyToPF.containsKey(pf.getName())) {
				if (overwrite) {
					pf.copyInto(keyToPF.get(pf.getName()));
				} else if (append && field.isNarrativeField()) {
					keyToPF.get(pf.getName()).appendValue(pf.getValue());
				}
			} else {
				field.getPrimitiveField().add(pf.deepCopy());
			}
		}
		
		
		Map<String, Field> keyToField = field.getKeyToFields();
		for (Field f : getFields()) {
			if (!keyToField.containsKey(f.getName())) {
				field.getFields().add(f.deepCopy(false));
			} 
		}
		return true;
	}

	/**
	 * Returns a deep copy of all things associated with the field.  
	 * IDs are not copied over
	 * 
	 * ONLY START AT ROOT
	 * 
	 * @return
	 */
	public Field deepCopy(boolean copyReferenceToAssessment) {
		Field field = new Field();
		field.setName(getName());
		
		if (copyReferenceToAssessment && this.getAssessment() != null) {
			field.assessment = assessment.deepCopy();
			field.assessment.setId(assessment.getId());
		}
		
		if (this.getNotes() != null) {
			field.setNotes(new HashSet<Notes>());
			for (Notes note : getNotes())
				field.getNotes().add(note.deepCopy());
		}
		
		if (this.getPrimitiveField() != null) {
			field.setPrimitiveField(new HashSet<PrimitiveField>());
			for (PrimitiveField pf : getPrimitiveField())
				field.getPrimitiveField().add(pf.deepCopy());
		}
		
		if (this.getFields() != null) {
			field.setFields(new HashSet<Field>());
			for (Field f : getFields()) {
				field.getFields().add(f.deepCopy(copyReferenceToAssessment));
			}
		}
		
		
		return field;
		
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Field) {
			Field field = (Field) obj;
			if (getName().equals(field.getName()) && 
					field.primitiveField.size() == getPrimitiveField().size() && 
					getFields().size() == field.getFields().size()) {
				Map<String, PrimitiveField> fieldPF = field.getKeyToPrimitiveFields();
				Map<String, Field> fieldF = field.getKeyToFields();
				for (PrimitiveField pf : getPrimitiveField())
					if (!(fieldPF.get(pf.getName()) != null && fieldPF.get(pf.getName()).equals(pf)))
						return false;
				for (Field f : getFields())
					if (! (fieldF.get(f.getName())!=null && fieldF.get(f.getName()).equals(f)))
						return false;
				return true;				
			}
		}
		return false;
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public java.util.Set<Field> getFields() {
		return fields;
	}

	public int getId() {
		return id;
	}

	public Map<String,Field> getKeyToFields() {
		HashMap<String, Field> keyToFields = new HashMap<String, Field>();
		for (Field f : getFields())
			keyToFields.put(f.getName(), f);

		return keyToFields;
	}

	public Map<String, PrimitiveField> getKeyToPrimitiveFields() {
		Map<String, PrimitiveField> keyToPrimitiveFields = new HashMap<String, PrimitiveField>();
		for (PrimitiveField pf : getPrimitiveField())
			keyToPrimitiveFields.put(pf.getName(), pf);
		
		return keyToPrimitiveFields;
		
	}

	public String getName() {
		return name;
	}

	public java.util.Set<Notes> getNotes() {
		return notes;
	}

	public int getORMID() {
		return getId();
	}

	public Field getParent() {
		return parent;
	}

	public java.util.Set<PrimitiveField> getPrimitiveField() {
		return primitiveField;
	}

	public java.util.Set<Reference> getReference() {
		return reference;
	}

	public boolean isNarrativeField() {
		return getName().endsWith("Documentation") || getName().equals(CanonicalNames.TaxonomicNotes) || getName().equals(CanonicalNames.RedListRationale);
	}

	public void setAssessment(Assessment value) {
		this.assessment = value;
	}

	public void setFields(java.util.Set<Field> value) {
		this.fields = value;
	}

	public void setId(int value) {
		this.id = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNotes(java.util.Set<Notes> value) {
		this.notes = value;
	}

	public void setParent(Field value) {
		this.parent = value;
	}

	public void setPrimitiveField(java.util.Set<PrimitiveField> value) {
		this.primitiveField = value;
	}

	public void setReference(java.util.Set<Reference> value) {
		this.reference = value;
	}

	public String toString() {
		return "FIELD " + String.valueOf(getId());
	}

	public String toXML() {
		StringBuilder str = new StringBuilder("<");
		str.append(ROOT_TAG);
		str.append(" name=\"");
		str.append(getName());
		str.append("\" id=\"");
		str.append(getId());
		str.append("\">\n");
		
		if( getFields().size() > 0 ) {
			str.append("<subfields>\n");
			for( Field subfield : getFields() )
				str.append(subfield.toXML());
			str.append("</subfields>\n");
		}
		
		for( PrimitiveField cur : getPrimitiveField() ) {
			str.append(cur.toXML());
		}
		
		if (getReference() != null) {
			for (Reference ref : getReference())
				str.append(ref.toXML());
		}
		
		if (getNotes() != null) {
			for (Notes note : getNotes())
				str.append(note.toXML());
		}
		
		str.append("</" + ROOT_TAG + ">\n");
		
		return str.toString();
	}

}
