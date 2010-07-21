package org.iucn.sis.server.crossport.demimport;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ServerReferenceUI extends ReferenceUI {

	public ServerReferenceUI(Element referenceElement) {
		super();

		this.referenceID = referenceElement.getAttribute("id");
		this.referenceType = referenceElement.getAttribute("type");
		NodeList children = referenceElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element currentField = (Element) children.item(i);
				if (currentField.getNodeName().equalsIgnoreCase("field")) {
					addField(currentField.getAttribute("name"), currentField.getTextContent());
				}
			}
		}
	}

}
