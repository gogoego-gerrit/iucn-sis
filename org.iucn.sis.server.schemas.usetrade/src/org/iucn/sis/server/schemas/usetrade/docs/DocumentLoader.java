package org.iucn.sis.server.schemas.usetrade.docs;

import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;

public class DocumentLoader {
	
	public static Document getView() {
		return BaseDocumentUtils.impl.getInputStreamFile(
			DocumentLoader.class.getResourceAsStream("views.xml")
		);
	}
	
	public static Document getField(String fieldName) {
		return BaseDocumentUtils.impl.getInputStreamFile(
			DocumentLoader.class.getResourceAsStream(fieldName + ".xml")
		);
	}

}
