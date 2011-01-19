package org.iucn.sis.shared.conversions;

import java.io.File;

import org.iucn.sis.shared.api.models.Definition;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class DefinitionConverter extends GenericConverter<String> {
	
	@Override
	protected void run() throws Exception {
		File file = new File(data + "/HEAD/browse/docs/definitions.xml");
		
		NativeDocument ndoc = new JavaNativeDocument();
		ndoc.parse(FileListing.readFileAsString(file));
		
		NativeNodeList nodes = ndoc.getDocumentElement().getElementsByTagName("definition");
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeElement el = nodes.elementAt(i);
			String name = el.getAttribute("id");
			String value = el.getTextContent();
			
			Definition definition = new Definition();
			definition.setName(name);
			definition.setValue(value);
			
			session.save(definition);
		}
	}

}
