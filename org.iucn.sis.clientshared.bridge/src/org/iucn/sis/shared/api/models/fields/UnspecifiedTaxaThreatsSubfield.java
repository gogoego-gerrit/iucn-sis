package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

public class UnspecifiedTaxaThreatsSubfield extends ThreatsSubfield {
	
	private static final long serialVersionUID = 1L;
	
	public UnspecifiedTaxaThreatsSubfield(ThreatsField parent, Field data) {
		super(parent, data);
	}
	
	public String getExplanation() {
		PrimitiveField<?> field = getPrimitiveField("text");
		if (field == null)
			return null;
		else
			return ((StringPrimitiveField)field).getValue();
	}
	
	public void setExplanation(String explanation) {
		addPrimitiveField(new StringPrimitiveField("text", this, explanation));
	}

}
