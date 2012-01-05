package org.iucn.sis.server.api.io;

import java.io.IOException;
import java.util.Map;

import javax.naming.NamingException;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.api.fields.TreeBuilder;
import org.iucn.sis.server.api.schema.AssessmentSchema;
import org.iucn.sis.server.api.schema.AssessmentSchemaFactory;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;

public class AssessmentSchemaIO {
	
	private final FieldSchemaGenerator generator;
	private final TreeBuilder treeBuilder;

	public AssessmentSchemaIO() {
		FieldSchemaGenerator generator;
		TreeBuilder builder;
		try {
			generator = new FieldSchemaGenerator();
			builder = new TreeBuilder();
		} catch (NamingException e) {
			generator = null;
			builder = null;
			Debug.println(e);
		}
		
		this.generator = generator;
		this.treeBuilder = builder;
	}
	
	public AssessmentSchema getAssessmentSchema(String name) throws ResourceException {
		AssessmentSchemaFactory factory;
		try {
			factory = SIS.get().getAssessmentSchemaBroker().getPlugin(name);
		} catch (Throwable e) {
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, e);
		}
		
		if (factory == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		try {
			return factory.newInstance();
		} catch (Throwable e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	public Document getFieldSchema(String field) throws Exception {
		return generator.getSchema(field);
	}
	
	public boolean hasGenerator() {
		return generator != null;
	}
	
	public Map<String, Map<Integer, String>> scanForLookups(String fieldName) throws Exception {
		return generator.scanForLookups(fieldName);
	}
	
	public String getFieldAsString(AssessmentSchema schema, String fieldName) throws IOException {
		Document document = schema.getField(fieldName);
		if (document == null) {
			Debug.println("FieldRestlet Error: Field {0} not found, skipping", fieldName);
			return "";
		}
		
		if (generator == null)
			return BaseDocumentUtils.impl.serializeDocumentToString(document, true, false);
		else {
			//Append field definition
			/*try {
				content.append(generator.getField(fieldName).toXML("definition"));
			} catch (Exception e) {
				e.printStackTrace();
				TrivialExceptionHandler.ignore(this, e);
			}*/
			final String dataType = document.getDocumentElement().getNodeName();
			if ("field".equals(dataType) || "tree".equals(dataType)) {
				//Append lookup table info
				Map<String, Map<Integer, String>> lookups = null;
				try {
					lookups = generator.scanForLookups(fieldName);
				} catch (Exception e) {
					Debug.println(e);
				}
				
				if (lookups != null) {
					for (Map.Entry<String, Map<Integer, String>> entry : lookups.entrySet()) {
						final Element lookupEl = document.createElement("lookup");
						lookupEl.setAttribute("id", entry.getKey());
						
						for (Map.Entry<Integer, String> options : entry.getValue().entrySet()) {
							final Element option = BaseDocumentUtils.impl.createCDATAElementWithText(
								document, "option", options.getValue()
							);
							option.setAttribute("id", options.getKey().toString());
							
							lookupEl.appendChild(option);
						}
						
						document.getDocumentElement().appendChild(lookupEl);
					}
				}
				
				if ("tree".equals(dataType)) {
					try {
						generator.appendCodesForClassificationScheme(document, fieldName);
					} catch (Exception e) {
						Debug.println(e);
					}
				}
			}
			else {
				final ElementCollection lookups = new ElementCollection(
					document.getDocumentElement().getElementsByTagName("lookup")
				);
				for (Element el : lookups) {
					final Map<Integer, String> data;
					try {
						data = generator.loadLookup(el.getAttribute("name"));
					} catch (Exception e) {
						Debug.println(e);
						continue;
					}
					
					if (data != null) {
						for (Map.Entry<Integer, String> options : data.entrySet()) {
							final Element option = BaseDocumentUtils.impl.createCDATAElementWithText(
								document, "option", options.getValue()
							);
							option.setAttribute("id", options.getKey().toString());
							
							el.appendChild(option);
						}
					}
				}
			}
			final ElementCollection coding = new ElementCollection(
				document.getDocumentElement().getElementsByTagName("coding")
			);
			for (Element el : coding) {
				if (CanonicalNames.Threats.equals(fieldName))
					treeBuilder.buildTree(el.getAttribute("name"), document, el);
				else
					treeBuilder.buildTree(el.getAttribute("name"), document, el.getParentNode());
			}
			
			return BaseDocumentUtils.impl.serializeDocumentToString(document, true, false);
		}
	}
	
}
