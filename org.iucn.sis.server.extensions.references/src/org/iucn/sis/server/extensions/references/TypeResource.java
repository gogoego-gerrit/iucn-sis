package org.iucn.sis.server.extensions.references;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

public class TypeResource extends Resource {

	public TypeResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(final Variant variant) {
		try {
			String type = (String) getRequest().getAttributes().get("type");
			try {
				if (type != null)
					type = URLDecoder.decode(type, "UTF-8");
			} catch (final UnsupportedEncodingException x) {
				throw new RuntimeException("Expected UTF-8 encoding support not found", x);
			}
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			@SuppressWarnings("unchecked")
			final Element rootEl = doc.createElement("reference");
			doc.appendChild(rootEl);
			rootEl.setAttribute("type", type);
			final ReferenceLabels labels = ReferenceLabels.loadFrom(getContext());
			final ReferenceLabels.LabelMappings mappings = labels.get(type);
			for (final String field : mappings.list()) {
				final String label = mappings.get(field);
				if (label != null && !"".equals(label) && !"ID".equals(field) && !"publication_type".equals(field)) {
					final Element fieldEl = doc.createElement("field");
					fieldEl.setAttribute("name", field);
					fieldEl.setAttribute("label", label);
					rootEl.appendChild(fieldEl);
				}
			}
			return new DomRepresentation(MediaType.TEXT_XML, doc);
		} catch (final ParserConfigurationException px) {
			px.printStackTrace();
			throw new RuntimeException("XML Parser not properly configured", px);
		}
	}
}
