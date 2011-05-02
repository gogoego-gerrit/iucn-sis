package files;

import java.io.InputStream;

import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;

public class Files {
	
	public static Document getXML(String name) {
		return BaseDocumentUtils.impl.getInputStreamFile(get(name));
	}
	
	public static InputStream get(String name) {
		return Files.class.getResourceAsStream(name);
	}

}
