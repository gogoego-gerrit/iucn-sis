package org.iucn.sis.server.etl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;

public class CaptureRLDB {

	public static void main(String[] args) throws Exception {

		DBSessionFactory.registerDataSource("source", "jdbc:access:///2007-red-list-decoded.mdb",
				"com.hxtt.sql.access.AccessDriver", "sa", "");

		ExecutionContext ec = new SystemExecutionContext("source");
		ec.setExecutionLevel(ExecutionContext.ADMIN);

		Document doc = ec.analyzeExistingStructure();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.transform(new DOMSource(doc), new StreamResult(System.out));
	}

}
