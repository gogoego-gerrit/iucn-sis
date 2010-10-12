package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.structures.DisplayStructure;

public class ThreatClassificationSchemeModelData extends
		ClassificationSchemeModelData {

	public ThreatClassificationSchemeModelData(DisplayStructure structure) {
		super(structure);
	}

	public ThreatClassificationSchemeModelData(DisplayStructure structure, Field field) {
		super(structure, field);
	}
	
	@Override
	public void save(Field parent, Field field) {
		structure.save(parent, field);
		
		field.setReference(references);
	}

}
