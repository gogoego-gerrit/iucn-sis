package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;

public class ViralThreatsSubfield extends ThreatsSubfield {
	
	private static final long serialVersionUID = 1L;

	public ViralThreatsSubfield(Field data) {
		super(data == null ? new Field() : data);
	}

	public Integer getVirus() {
		PrimitiveField<?> field = proxy.getPrimitiveField("virus");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setVirus(Integer virus) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("virus", proxy, virus, null));
	}
	
}
