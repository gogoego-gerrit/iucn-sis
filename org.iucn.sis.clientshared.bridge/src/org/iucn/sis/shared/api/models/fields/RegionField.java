package org.iucn.sis.shared.api.models.fields;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class RegionField extends Field {
	
	private static final long serialVersionUID = 1L;
	
	public static String CANONICAL_NAME = CanonicalNames.RegionInformation;
	public static String PRIMITIVE_FIELD = "regions";
	
	@SuppressWarnings("unchecked")
	public RegionField(boolean isEndemic, List<Integer> regionIDs, Assessment assessment) {
		super(CANONICAL_NAME, assessment);
		
		final Set<PrimitiveField> fields = new HashSet<PrimitiveField>();
		fields.add(new BooleanPrimitiveField("endemic", this, isEndemic));
		
		final ForeignKeyListPrimitiveField list = 
			new ForeignKeyListPrimitiveField(PRIMITIVE_FIELD, this, CANONICAL_NAME + "_regionsLookup");
		list.setValue(regionIDs);
		fields.add(list);
		
		setPrimitiveField(fields);
	}

}
