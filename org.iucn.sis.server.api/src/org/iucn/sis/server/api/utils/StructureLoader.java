package org.iucn.sis.server.api.utils;

import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;

public class StructureLoader {
	
	public static Document loadPostgres() {
		return load("struct-postgresql.xml");
	}
	
	public static Document load(String name) {
		return BaseDocumentUtils.impl.getInputStreamFile(StructureLoader.class.getResourceAsStream(name));
	}

}
