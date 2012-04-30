package org.iucn.sis.server.api.schema.redlist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.debug.Debug;
import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class DocumentLoader {
	
	private static final VFSPath FIELDS_DIR = 
		new VFSPath("/browse/docs/fields/org.iucn.sis.server.schemas.redlist");
	
	public static Document getView() {
		final VFSPathToken token = new VFSPathToken("views.xml");
		if (SIS.get().getVFS().exists(FIELDS_DIR.child(token))) {
			try {
				return SIS.get().getVFS().getMutableDocument(FIELDS_DIR.child(token));
			} catch (IOException e) {
				Debug.println("View reported existence, but could not be loaded:\n{0}", e);
			}
		}
		
		return BaseDocumentUtils.impl.getInputStreamFile(
			DocumentLoader.class.getResourceAsStream("views.xml")
		);
	}
	
	public static Document getField(String fieldName) {
		final VFSPathToken token = new VFSPathToken(fieldName + ".xml");
		if (SIS.get().getVFS().exists(FIELDS_DIR.child(token))) {
			try {
				return getInputStreamFile(SIS.get().getVFS().getInputStream(FIELDS_DIR.child(token)));
			} catch (IOException e) {
				Debug.println("Field {0} reported existence, but could not be loaded:\n{1}", fieldName, e);
			}
		}
		
		return getInputStreamFile(
			DocumentLoader.class.getResourceAsStream(fieldName + ".xml")
		);
	}
	
	private static Document getInputStreamFile(InputStream stream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = "", xml = "";
			while ((line = reader.readLine()) != null)
				xml += line;
			reader.close();
			return BaseDocumentUtils.impl.createDocumentFromString(xml);
		} catch (Exception e) {
			return null;
		}
	}

}
