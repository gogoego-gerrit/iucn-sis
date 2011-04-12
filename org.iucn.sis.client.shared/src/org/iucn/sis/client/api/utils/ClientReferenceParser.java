package org.iucn.sis.client.api.utils;

import org.iucn.sis.client.api.caches.ReferenceCache;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.parsers.ReferenceParser;

import com.solertium.lwxml.shared.NativeNode;

public class ClientReferenceParser extends ReferenceParser {
	
	@Override
	public Reference parse(NativeNode element) throws IllegalArgumentException {
		return parse(element, false);
	}
	
	@Override
	public Reference parse(NativeNode element, boolean allowNew) {
		Reference reference = super.parse(element, allowNew);
		if (reference.getId() > 0)
			ReferenceCache.impl.cache(reference);
		return reference;
	}

}
