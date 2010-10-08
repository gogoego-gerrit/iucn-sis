package org.iucn.sis.shared.api.models.fields;

import java.util.HashSet;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class ThreatsField extends Field {
	
	private static final long serialVersionUID = 1L;
	
	public ThreatsField() {
		super(CanonicalNames.Threats, null);
	}
	
	public void addSubfield(ThreatsSubfield field) {
		if (getFields() == null)
			setFields(new HashSet<Field>());
		
		getFields().add(field);
	}
	
	/**
	 * Add the subfields.  No primitive fields at this 
	 * level, so we are only concerned about the subfields.
	 * @param field
	 */
	public void parse(Field field) {
		if (field != null)
			setFields(field.getFields());
	}

}
