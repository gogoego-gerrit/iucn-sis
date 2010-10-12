package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;

public class IASTaxaThreatsSubfield extends ThreatsSubfield {
	
	private static final long serialVersionUID = 1L;

	public IASTaxaThreatsSubfield(Field data) {
		super(data);
	}
	
	public Integer getIASTaxa() {
		PrimitiveField<?> field = proxy.getPrimitiveField("ias");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setIASTaxa(Integer taxaID) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("ias", proxy, taxaID, null));
	}
	
	public Integer getAncestry() {
		PrimitiveField<?> field = proxy.getPrimitiveField("ancestry");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setAncestry(Integer ancestry) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("ancestry", proxy, ancestry, null));
	}

}
