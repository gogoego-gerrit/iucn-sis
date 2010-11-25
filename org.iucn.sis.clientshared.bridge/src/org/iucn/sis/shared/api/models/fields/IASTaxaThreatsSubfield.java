package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;

public class IASTaxaThreatsSubfield extends ThreatsSubfield {
	
	private static final long serialVersionUID = 1L;

	public IASTaxaThreatsSubfield(Field data) {
		super(data);
	}
	
	public Integer getIASTaxa() {
		return getForeignKeyPrimitiveField("ias");
	}
	
	public void setIASTaxa(Integer taxaID) {
		setForeignKeyPrimitiveField("ias", taxaID);
	}
	
	public Integer getAncestry() {
		return getForeignKeyPrimitiveField("ancestry");
	}
	
	public void setAncestry(Integer ancestry) {
		setForeignKeyPrimitiveField("ancestry", ancestry);
	}

}
