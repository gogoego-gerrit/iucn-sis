package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class RedListPublicationField extends Field{
	
	public static String CANONICAL_NAME = CanonicalNames.RedListPublication;
	
	public RedListPublicationField() {
		setName(CANONICAL_NAME);
	}

}
