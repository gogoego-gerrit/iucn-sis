package org.iucn.sis.shared.api.models.fields;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class RegionField {
	
	private final Field proxy;
	
	public RegionField() {
		this(null);
	}
	
	public RegionField(Field field) {
		this.proxy = field == null ? new Field(CanonicalNames.RegionInformation, null) : field;
	}
	
	public void setEndemic(Boolean isEndemic) {
		PrimitiveField<?> field = proxy.getPrimitiveField("endemic");
		if (field != null) {
			if (isEndemic != null)
				((BooleanPrimitiveField)field).setValue(isEndemic);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (isEndemic != null && !isEndemic.equals(Boolean.FALSE))
			proxy.addPrimitiveField(new BooleanPrimitiveField("endemic", proxy, isEndemic));
		else
			proxy.getPrimitiveField().remove(field);	
	}
	
	public boolean isEndemic() {
		PrimitiveField<?> field = proxy.getPrimitiveField("endemic");
		if (field != null)
			return ((BooleanPrimitiveField)field).getValue();
		else
			return false;
	}
	
	public void setRegions(List<Integer> regionIDs) {
		PrimitiveField<?> field = proxy.getPrimitiveField("regions");
		if (field != null)
			((ForeignKeyListPrimitiveField)field).setValue(regionIDs);
		else {
			field = new ForeignKeyListPrimitiveField("regions", proxy, "region");
			((ForeignKeyListPrimitiveField)field).setValue(regionIDs);
			
			proxy.addPrimitiveField(field);
		}
	}
	
	public List<Integer> getRegionIDs() {
		PrimitiveField<?> field = proxy.getPrimitiveField("regions");
		if (field == null)
			return new ArrayList<Integer>();
		else
			return ((ForeignKeyListPrimitiveField)field).getValue();
	}

}
