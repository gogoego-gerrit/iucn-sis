package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class RedListEvaluatedField extends Field {
	
	private static final long serialVersionUID = 1L;
	
	public RedListEvaluatedField(Assessment assessment) {
		super(CanonicalNames.RedListEvaluated, assessment);
	}
	
	public void load(Field field) {
		if (field != null)
			setPrimitiveField(field.getPrimitiveField());
	}
	
	public boolean isEvaluated() {
		PrimitiveField field = getPrimitiveField("isEvaluated");
		Boolean value;
		if (field != null && field instanceof BooleanPrimitiveField)
			value = ((BooleanPrimitiveField)field).getValue();
		else
			value = null;
		
		return value != null && value.booleanValue();
	}
	
	public Integer getSuccessStatus() {
		return getFKValue("status");
	}
	
	public boolean hasPassed() {
		Integer value = getSuccessStatus();
		return value != null && 1 == value;
	}

	private Integer getFKValue(String name) {
		PrimitiveField field = getPrimitiveField(name);
		if (field != null && field instanceof ForeignKeyPrimitiveField)
			return ((ForeignKeyPrimitiveField)field).getValue();
		else
			return null;
	}
}
