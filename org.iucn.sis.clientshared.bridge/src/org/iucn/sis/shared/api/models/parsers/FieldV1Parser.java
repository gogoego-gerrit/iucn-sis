package org.iucn.sis.shared.api.models.parsers;

import java.util.HashSet;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.NativeDocumentSerializer;

public class FieldV1Parser {
	
	public static Field parse(final NativeElement element) {
		final Field field = new Field();
		field.setName(element.getAttribute("name"));
		
		final String id = element.getAttribute("id");
		try {
			field.setId(Integer.valueOf(id));
		} catch (NumberFormatException e) {
			Debug.println("ERROR - FIELD {0} DOES NOT HAVE AN ID!!! " +
				"trying to parse {1} with name {0}", field.getName(), id);
		}
		
		final NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode current = nodes.item(i);
			if ("subfields".equals(current.getNodeName())) {
				NativeNodeList subsubFields = current.getChildNodes();
				for (int k = 0; k < subsubFields.getLength(); k++) {
					NativeNode possibleSubField = subsubFields.item(k);
					if (possibleSubField instanceof NativeElement) {
						Field subfield = Field.fromXML((NativeElement)possibleSubField);
						subfield.setParent(field);
						
						field.getFields().add(subfield);
					}
				}
			}
			else if (PrimitiveField.ROOT_TAG.equals(current.getNodeName())) {
				NativeElement primEl = (NativeElement)current;
				PrimitiveField cur = PrimitiveFieldFactory.
					generatePrimitiveField(primEl.getAttribute(PrimitiveField.TYPE_TAG));
				cur.fromXML(primEl);
				cur.setField(field);
				field.getPrimitiveField().add(cur);
			}
			else if (Notes.ROOT_TAG.equals(current.getNodeName())) {
				Notes cur = Notes.fromXML((NativeElement)current);
				cur.setField(field);
				
				field.getNotes().add(cur);
			}
			else if (Reference.ROOT_TAG.equals(current.getNodeName())) {
				Reference cur = Reference.fromXML(current);
				cur.getField().add(field);
				
				field.getReference().add(cur);
			}
		}
		
		return field;
	}

}
