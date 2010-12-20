package org.iucn.sis.server.restlets.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.api.fields.TreeBuilder;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.schema.AssessmentSchema;
import org.iucn.sis.server.api.schema.AssessmentSchemaBroker;
import org.iucn.sis.server.api.schema.AssessmentSchemaFactory;
import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.NotFoundException;

public class AssessmentSchemaRestlet extends BaseServiceRestlet {
	
	private final FieldSchemaGenerator generator;
	private final TreeBuilder treeBuilder;

	public AssessmentSchemaRestlet(Context context) {
		super(context);
		
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

	@Override
	public void definePaths() {
		paths.add("/application/schema");
		paths.add("/application/schema/{name}/view");
		paths.add("/application/schema/{name}/field");
		paths.add("/application/schema/{name}/field/{fieldList}");
		paths.add("/application/schema/{name}/field/{fieldList}/schema");
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		String schemaName = getName(request);
		if (schemaName == null)
			return listSchemas();
		
		AssessmentSchema schema = getSchema(schemaName);
			
		if ("view".equals(request.getResourceRef().getLastSegment()))
			return new DomRepresentation(MediaType.TEXT_XML, schema.getViews());
		
		List<String> fieldList = getFieldList(request);
		if ("schema".equals(request.getResourceRef().getLastSegment()))
			return getSchemas(schema, fieldList);
		else 
			return getFields(schema, fieldList);
	}
	
	@Override
	public void handlePost(Representation rep, Request request, Response response) throws ResourceException {
		String schemaName = getName(request);
		if (schemaName == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide an assessment schema.");
		
		AssessmentSchema schema = getSchema(schemaName);
		
		final Document entity = getEntityAsDocument(rep);
		
		final List<String> fields = new ArrayList<String>();
		
		final NodeCollection nodes = new NodeCollection(
			entity.getDocumentElement().getChildNodes()
		);
		for (Node node : nodes) {
			if ("field".equals(node.getNodeName()))
				fields.add(node.getTextContent());
		}
		
		response.setEntity(getFields(schema, fields));
		response.setStatus(Status.SUCCESS_OK);
	}
	
	private Representation getFields(AssessmentSchema schema, List<String> fieldNames) {
		StringBuilder ret = new StringBuilder("<fields>\r\n");
		for (String field : fieldNames) {
			try {
				ret.append(getFieldAsString(schema, field));
			} catch (NotFoundException e) {
				Debug.println("Field {0} not found!  Skipping...", field);
			} catch (IOException e) {
				Debug.println("Field {0} not added!  Skipping...", field);
			}
		}
		ret.append("</fields>");
		
		return new StringRepresentation(ret.toString(), MediaType.TEXT_XML);
	}
	
	private Representation getSchemas(AssessmentSchema schema, List<String> fieldNames) {
		StringBuilder ret = new StringBuilder("<fields>\r\n");
		for (String field : fieldNames) {
			try {
				ret.append(BaseDocumentUtils.impl.serializeDocumentToString(
					generator.getSchema(field), true, false));
			} catch (NotFoundException e) {
				Debug.println("Field {0} not found!  Skipping...", field);
			} catch (IOException e) {
				Debug.println("Field {0} not added!  Skipping...", field);
			} catch (Exception e) {
				Debug.println("Field {0} not added!  Skipping...", field);
			}
		}
		ret.append("</fields>");
		
		return new StringRepresentation(ret.toString(), MediaType.TEXT_XML);
	}
	
	private String getFieldAsString(AssessmentSchema schema, String fieldName) throws IOException {
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
				treeBuilder.buildTree(el.getAttribute("name"), document, el);
			}
			
			return BaseDocumentUtils.impl.serializeDocumentToString(document, true, false);
		}
	}
	
	private Representation listSchemas() throws ResourceException {
		AssessmentSchemaBroker broker = SIS.get().getAssessmentSchemaBroker();
		Map<String, AssessmentSchemaFactory> map;
		try {
			map = broker.getPlugins();
		} catch (Throwable e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
		
		final String defaultSchema = SIS.get().getDefaultSchema();
		
		StringBuilder builder = new StringBuilder();
		builder.append("<root>");
		
		for (Map.Entry<String, AssessmentSchemaFactory> entry : map.entrySet()) {
			AssessmentSchema schema;
			try {
				schema = entry.getValue().newInstance();
			} catch (Throwable e) {
				Debug.println("Could not create instance of assessment schema {0}", entry.getKey());
				continue;
			}
			
			builder.append("<row>");
			builder.append(writeField("id", entry.getKey()));
			builder.append(writeField("name", schema.getName()));
			builder.append(writeField("description", schema.getDescription()));
			if (defaultSchema.equals(entry.getKey()))
				builder.append(writeField("default", "true"));
			builder.append("</row>");
		}
		
		builder.append("</root>");
		
		return new StringRepresentation(builder.toString(), MediaType.TEXT_XML);
	}
	
	private String writeField(String name, String value) {
		return "<field name=\"" + name + "\"><![CDATA[" + value + "]]></field>";
	}
	
	private String getName(Request request) {
		return getAttribute("name", request);
	}
	
	private List<String> getFieldList(Request request) {
		String list = getAttribute("fieldList", request);
		return Arrays.asList(list.split(","));
	}
	
	private String getAttribute(String attribute, Request request) {
		return (String)request.getAttributes().get(attribute);
	}
	
	private AssessmentSchema getSchema(String name) throws ResourceException {
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

}
