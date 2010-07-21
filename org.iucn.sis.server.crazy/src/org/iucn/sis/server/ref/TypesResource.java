package org.iucn.sis.server.ref;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TypesResource extends Resource {

	public TypesResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(final Variant variant) {
		try {
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			@SuppressWarnings("unchecked")
			final Element rootEl = doc.createElement("reference-types");
			doc.appendChild(rootEl);
			for (final String type : ReferenceLabels.loadFrom(getContext()).listTypes()) {
				final Element typeEl = doc.createElement("type");
				typeEl.appendChild(doc.createTextNode(type));
				rootEl.appendChild(typeEl);
			}
			return new DomRepresentation(MediaType.TEXT_XML, doc);
		} catch (final ParserConfigurationException px) {
			px.printStackTrace();
			throw new RuntimeException("XML Parser not properly configured", px);
		}
	}
}
