package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;

public class IASTaxaThreatsSubfield extends ThreatsSubfield {
	
	private static final long serialVersionUID = 1L;

	public IASTaxaThreatsSubfield(ThreatsField parent, Field data) {
		super(parent, data);
	}
	
	public Integer getIASTaxa() {
		PrimitiveField<?> field = getPrimitiveField("ias");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setIASTaxa(Integer taxaID) {
		addPrimitiveField(new ForeignKeyPrimitiveField("ias", this, taxaID, null));
	}
	
	public Integer getAncestry() {
		PrimitiveField<?> field = getPrimitiveField("ancestry");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setAncestry(Integer ancestry) {
		addPrimitiveField(new ForeignKeyPrimitiveField("ancestry", this, ancestry, null));
	}

}
