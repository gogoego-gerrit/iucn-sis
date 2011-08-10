package org.iucn.sis.shared.api.models.fields;

import java.util.List;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class RegionField extends ProxyField {
	
	public RegionField() {
		this(null);
	}
	
	public RegionField(Field field) {
		super(field == null ? new Field(CanonicalNames.RegionInformation, null) : field);
	}
	
	public void setEndemic(Boolean isEndemic) {
		setBooleanPrimitiveField("endemic", isEndemic, Boolean.FALSE);	
	}
	
	public boolean isEndemic() {
		return getBooleanPrimitiveField("endemic", Boolean.FALSE);
	}
	
	public void setRegions(List<Integer> regionIDs) {
		setForeignKeyListPrimitiveField("regions", regionIDs, "region");
	}
	
	public List<Integer> getRegionIDs() {
		return getForeignKeyListPrimitiveField("regions");
	}

}
