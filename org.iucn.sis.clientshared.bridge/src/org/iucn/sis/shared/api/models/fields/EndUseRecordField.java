package org.iucn.sis.shared.api.models.fields;

import java.util.List;

import org.iucn.sis.shared.api.models.Field;

public class EndUseRecordField extends ProxyField {
	
	public EndUseRecordField(Field field) {
		super(field);
	}
	
	public void setEndUse(Integer value) {
		setForeignKeyPrimitiveField("endUse", value);
	}
	
	public Integer getEndUse() {
		return getForeignKeyPrimitiveField("endUse");
	}
	
	public void setBiologicalProduct(String value) {
		setTextPrimitiveField("biologicalPart", value);
	}
	
	public String getBiologicalProduct() {
		return getTextPrimitiveField("biologicalPart");
	}
	
	public void setScale(List<Integer> value) {
		setForeignKeyListPrimitiveField("scale", value);
	}
	
	public List<Integer> getScale() {
		return getForeignKeyListPrimitiveField("scale");
	}
	
	public void setDriverOfHarvest(Integer value) {
		setForeignKeyPrimitiveField("driver", value);
	}
	
	public Integer getDriverOfHarvest() {
		return getForeignKeyPrimitiveField("driver");
	}
	
	public void setSignificantRisk(Integer value) {
		setForeignKeyPrimitiveField("significantRisk", value);
	}
	
	public Integer getSignificantRisk() {
		return getForeignKeyPrimitiveField("significantRisk");
	}
	
	public void setDocumentation(String value) {
		setTextPrimitiveField("documentation", value);
	}
	
	public String getDocumentation() {
		return getTextPrimitiveField("documentation");
	}

}
