package org.iucn.sis.server.api.fields.definitions;

import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;

public class FieldDefinitionLoader {
	
	public static Document get(String fieldName) {
		String file = fieldName;
		if (!fieldName.endsWith(".xml"))
			file += ".xml";
		return BaseDocumentUtils.impl.getInputStreamFile(
			FieldDefinitionLoader.class.getResourceAsStream(file)
		);
	}

}
