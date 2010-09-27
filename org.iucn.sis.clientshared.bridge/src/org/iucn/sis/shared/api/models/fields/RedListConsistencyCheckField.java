package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class RedListConsistencyCheckField extends Field {
	
	private static final long serialVersionUID = 1L;
	
	public RedListConsistencyCheckField(Assessment assessment) {
		super(CanonicalNames.RedListConsistencyCheck, assessment);
	}
	
	public void load(Field field) {
		if (field != null)
			setPrimitiveField(field.getPrimitiveField());
	}
	
	public Integer getSuccessStatus() {
		return getFKValue("successStatus");
	}
	
	public boolean hasPassed() {
		Integer value = getSuccessStatus();
		return value != null && 1 == value;
	}
	
	public Integer getProgress() {
		return getFKValue("progress");
	}
	
	public boolean isProgressComplete() {
		Integer value = getProgress();
		return value != null && 2 == value;
	}
	
	private Integer getFKValue(String name) {
		PrimitiveField field = getPrimitiveField(name);
		if (field != null && field instanceof ForeignKeyPrimitiveField)
			return ((ForeignKeyPrimitiveField)field).getValue();
		else
			return null;
	}

}
