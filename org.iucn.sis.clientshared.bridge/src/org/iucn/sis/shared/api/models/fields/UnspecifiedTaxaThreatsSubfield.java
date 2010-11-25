package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;

public class UnspecifiedTaxaThreatsSubfield extends ThreatsSubfield {
	
	private static final long serialVersionUID = 1L;
	
	public UnspecifiedTaxaThreatsSubfield(Field data) {
		super(data);
	}
	
	public String getExplanation() {
		return getStringPrimitiveField("text");
	}
	
	public void setExplanation(String explanation) {
		setStringPrimitiveField("text", explanation);
	}

}
