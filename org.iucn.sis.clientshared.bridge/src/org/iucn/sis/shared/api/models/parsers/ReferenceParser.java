package org.iucn.sis.shared.api.models.parsers;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Reference;

import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class ReferenceParser {
	
	public Reference parse(NativeNode element) throws IllegalArgumentException {
		return parse(element, false);
	}
	
	public Reference parse(NativeNode element, boolean allowNew) {
		final Reference reference = new Reference();
		reference.setId(-1);
		
		final NativeNodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode field = nodes.item(i);
			final String name = field.getNodeName();
			final String value = field.getTextContent();
	
			Reference.addField(reference, name, value);
		}	
		
		if (!allowNew && reference.getId() <= 0)
			throw new IllegalArgumentException("Error building reference from node, required fields not present.");
		
		if (reference.getType() == null) {
			Debug.println("Reference type null for {0}, setting to 'other'", reference.getId());
			reference.setType("other");
		}
		
		return reference;
	}

}
