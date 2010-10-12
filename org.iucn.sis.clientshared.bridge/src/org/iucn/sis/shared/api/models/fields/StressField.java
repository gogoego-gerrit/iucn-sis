package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;

public class StressField {
	
	private static final long serialVersionUID = 1L;
	
	private Field proxy;

	public StressField(Field data) {
		this.proxy = data;
	}

	public void setStress(Integer stressID) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("stress", proxy, stressID, null));
	}
	
	public Integer getStress() {
		PrimitiveField<?> field = proxy.getPrimitiveField("stress");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public Field getField() {
		return proxy;
	}
	
}
