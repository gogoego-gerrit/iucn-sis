package files;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.w3c.dom.Document;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.BaseDocumentUtils;

public class Files {
	
	public static NativeDocument getNativeDocument(String name) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(get(name)));
		StringBuilder in = new StringBuilder();
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null)
				in.append(line);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		NativeDocument document = new JavaNativeDocument();
		document.parse(in.toString());
		
		return document;
	}
	
	public static Document getXML(String name) {
		return BaseDocumentUtils.impl.getInputStreamFile(get(name));
	}
	
	public static InputStream get(String name) {
		return Files.class.getResourceAsStream(name);
	}

}
