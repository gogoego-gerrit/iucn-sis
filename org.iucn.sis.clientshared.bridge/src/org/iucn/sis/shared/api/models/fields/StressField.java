package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;

public class StressField extends ProxyField {
	
	private static final long serialVersionUID = 1L;

	public StressField(Field data) {
		super(data);
	}

	public void setStress(Integer stressID) {
		setForeignKeyPrimitiveField("stress", stressID);
	}
	
	public Integer getStress() {
		return getForeignKeyPrimitiveField("stress");
	}
	
}
