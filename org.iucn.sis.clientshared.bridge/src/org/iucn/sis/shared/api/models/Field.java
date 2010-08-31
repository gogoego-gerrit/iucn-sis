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

import org.iucn.sis.shared.api.models.parsers.FieldV1Parser;
import org.iucn.sis.shared.api.models.parsers.FieldV2Parser;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class Field implements Serializable {

	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	//public static final String ROOT_TAG = "field";
	
	private static final String VERSION = "2";

	public static Field fromXML(NativeElement element) {
		String version = element.getAttribute("version");
		if (VERSION.equals(version))
			return FieldV2Parser.parse(element); 
		else
			return FieldV1Parser.parse(element);
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
	
	public PrimitiveField<?> getPrimitiveField(String fieldName) {
		if (primitiveField == null)
			primitiveField = new HashSet<PrimitiveField>();
		
		return getKeyToPrimitiveFields().get(fieldName);
	}
	
	public void addPrimitiveField(PrimitiveField<?> field) {
		if (primitiveField == null)
			primitiveField = new HashSet<PrimitiveField>();
		
		PrimitiveField<?> existing = getPrimitiveField(field.getName());
		if (existing == null)
			primitiveField.remove(existing);
		
		primitiveField.add(field);
	}
	
	public Field getField(String fieldName) {
		if (fields == null)
			fields = new HashSet<Field>();
		
		return getKeyToFields().get(fieldName);
	}
	
	public void addField(Field field) {
		if (fields == null)
			fields = new HashSet<Field>();
		
		Field existing = getField(field.getName());
		if (existing != null)
			fields.remove(existing);
		
		fields.add(field);
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
		/*
		 * FIXME: is this necessary?
		 */
		if (fields != null)
			for (Field field : fields)
				field.setAssessment(value);
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
		return toXML(getName());
	}
	
	public String toXML(String rootTag) {
		StringBuilder str = new StringBuilder("<");
		str.append(rootTag);
		str.append(" version=\"");
		str.append(VERSION);
		str.append("\"");
		str.append(" id=\"");
		str.append(getId());
		str.append("\">\n");
		
		if( getFields().size() > 0 ) {
			str.append("<subfields>\n");
			for( Field subfield : getFields() )
				str.append(subfield.toXML());
			str.append("</subfields>\n");
		}
		
		for (PrimitiveField<?> cur : getPrimitiveField()) {
			str.append(cur.toXML());
			str.append("\n");
		}
		
		if (getReference() != null) {
			for (Reference ref : getReference())
				str.append(ref.toXML()+"\n");
		}
		
		if (getNotes() != null) {
			for (Notes note : getNotes())
				str.append(note.toXML()+"\n");
		}
		
		str.append("</" + rootTag + ">\n");
		
		return str.toString();
	}

}
