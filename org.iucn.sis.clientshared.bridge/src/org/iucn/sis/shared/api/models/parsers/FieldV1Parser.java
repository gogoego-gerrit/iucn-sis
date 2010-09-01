package org.iucn.sis.shared.api.models.parsers;

import java.util.HashSet;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class FieldV1Parser {
	
	public static Field parse(final NativeElement element) {
		Field field = new Field();
		
		String name = element.getAttribute("name");
		field.setName(name);
		String id = element.getAttribute("id");
		try {
		field.setId(Integer.valueOf(id).intValue());
		} catch (NumberFormatException e) {
			Debug.println("ERROR - FIELD " + name + " DOES NOT HAVE AN ID!!! trying to parse " + id + " with name " + name);
		}
		
		field.setFields(new HashSet<Field>());
		NativeNodeList subfields = element.getElementsByTagName("subfields");
		if (subfields.getLength() > 0) {
			NativeNodeList subsubFields = subfields.elementAt(0).getElementsByTagName("field");
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

}
