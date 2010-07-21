package org.iucn.sis.server.extensions.references;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReferenceServerUnitTester {

	public static void main(String[] args) throws Exception {
		Client c = new Client(Protocol.HTTP);
		Response r = c.get("http://127.0.0.1:41141/reference/0079C44FA705C6314B6073E6766011E");
		DomRepresentation domr = new DomRepresentation(r.getEntity());
		Document idoc = domr.getDocument();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new DOMSource(idoc), new StreamResult(System.out));
		System.out.println("\n\n");

		Element el1 = (Element) idoc.getElementsByTagName("field").item(1);
		el1.setTextContent("McMurtry, Foo");
		t.transform(new DOMSource(idoc), new StreamResult(System.out));
		System.out.println("\n\n");

		Response r2 = c.post("http://127.0.0.1:41141/submit", new DomRepresentation(MediaType.TEXT_XML, idoc));
		DomRepresentation domr2 = new DomRepresentation(r2.getEntity());
		Document idoc2 = domr2.getDocument();
		t.transform(new DOMSource(idoc2), new StreamResult(System.out));
	}

}
