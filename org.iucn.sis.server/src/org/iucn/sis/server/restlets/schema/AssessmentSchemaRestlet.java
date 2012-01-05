package org.iucn.sis.server.restlets.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentSchemaIO;
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
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.NotFoundException;

public class AssessmentSchemaRestlet extends BaseServiceRestlet {
	
	private final AssessmentSchemaIO io;

	public AssessmentSchemaRestlet(Context context) {
		super(context);
		
		io = new AssessmentSchemaIO();
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
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		String schemaName = getName(request);
		if (schemaName == null)
			return listSchemas();
		
		AssessmentSchema schema = io.getAssessmentSchema(schemaName);
			
		if ("view".equals(request.getResourceRef().getLastSegment()))
			return new DomRepresentation(MediaType.TEXT_XML, schema.getViews());
		
		List<String> fieldList = getFieldList(request);
		if ("schema".equals(request.getResourceRef().getLastSegment()))
			return getSchemas(schema, fieldList);
		else 
			return getFields(schema, fieldList);
	}
	
	@Override
	public void handlePost(Representation rep, Request request, Response response, Session session) throws ResourceException {
		String schemaName = getName(request);
		if (schemaName == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide an assessment schema.");
		
		AssessmentSchema schema = io.getAssessmentSchema(schemaName);
		
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
			String name = field;
			if (name.endsWith(".xml"))
				name = name.substring(0, name.lastIndexOf('.'));
			try {
				ret.append(getFieldAsString(schema, name));
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
					io.getFieldSchema(field), true, false));
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
		return io.getFieldAsString(schema, fieldName);
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

}
