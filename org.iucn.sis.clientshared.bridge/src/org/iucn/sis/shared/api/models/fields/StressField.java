package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;

public class StressField extends Field {
	
	private static final long serialVersionUID = 1L;

	public StressField() {
		super("StressesSubfield", null);
	}

	public void setStress(Integer stressID) {
		addPrimitiveField(new ForeignKeyPrimitiveField("stress", this, stressID, null));
	}
	
	public Integer getStress() {
		PrimitiveField<?> field = getPrimitiveField("stress");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
}
