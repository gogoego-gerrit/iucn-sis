package org.iucn.sis.server.extensions.fieldmanager.restlets;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.schema.AssessmentSchema;
import org.iucn.sis.server.api.schema.AssessmentSchemaBroker;
import org.iucn.sis.server.api.schema.AssessmentSchemaFactory;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;

public class FieldManagerRestlet extends BaseServiceRestlet {
	
	private final FieldSchemaGenerator generator;
	private final ExecutionContext ec;

	public FieldManagerRestlet(Context context, FieldSchemaGenerator generator) {
		super(context);
		
		this.generator = generator;
		this.ec = generator.getExecutionContext();
	}

	@Override
	public void definePaths() {
		paths.add("/application/manager");
		paths.add("/application/manager/{schema}/{mode}/{fieldName}");
		paths.add("/application/manager/{schema}/{mode}/{fieldName}/{prim}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		String s = (String)request.getAttributes().get("schema");
		String mode = (String)request.getAttributes().get("mode");
		String fieldName = (String)request.getAttributes().get("fieldName");
		
		if (s == null) {
			AssessmentSchemaBroker broker = SIS.get().getAssessmentSchemaBroker();
			Map<String, AssessmentSchemaFactory> map;
			try {
				map = broker.getPlugins();
			} catch (Throwable e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
			}
			
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
				builder.append("</row>");
			}
			
			builder.append("</root>");
			
			return new StringRepresentation(builder.toString(), MediaType.TEXT_XML);
		}
		
		AssessmentSchema schema = getSchema(s);
		
		String table = schema.getTablePrefix() + fieldName;
		
		Field field = getField(table);
		if (field == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
	
		Map<String, Map<Integer, String>> lookups = null;
		try {
			lookups = generator.scanForLookups(table);
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		StringBuilder xml = new StringBuilder();
		xml.append("<root>");
		xml.append(field.toXML());
		if (lookups != null) {
			for (Map.Entry<String, Map<Integer, String>> entry : lookups.entrySet()) {
				xml.append("<lookup id=\"" + entry.getKey() + "\">");
				for (Map.Entry<Integer, String> options : entry.getValue().entrySet())
					xml.append("<option id=\"" + options.getKey() + "\"><![CDATA[" + options.getValue() + "]]></option>");
				xml.append("</lookup>");
			}
		}
		xml.append("</root>");
		
		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
	}
	
	private String writeField(String name, String value) {
		return "<field name=\"" + name + "\"><![CDATA[" + value + "]]></field>";
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
		AssessmentSchema schema = getSchema((String)request.getAttributes().get("schema"));
		String fieldName = (String)request.getAttributes().get("fieldName");
		String mode = (String)request.getAttributes().get("mode");
		
		String tableName = ("lookup".equals(mode) ? "" : schema.getTablePrefix()) + fieldName;
		
		ArrayList<String> exist;
		try {
			exist = ec.getMatchingTables(tableName);
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,e);
		}
		if (!exist.isEmpty())
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "The table " + fieldName + " already exists.");
		
		String createSQL;
		if ("lookup".equals(mode)) {
			createSQL = "CREATE TABLE " + tableName + " " + 
				"(id integer auto_increment primary key, name varchar(255), label varchar(255))";
		}
		else {
			createSQL = "CREATE TABLE " + tableName + " " + 
				"(id integer auto_increment primary key, name varchar(255), data_type varchar(255), number_allowed varchar(255))";
		}
		
		runQuery(createSQL);
		
		response.setEntity(createSQL, MediaType.TEXT_PLAIN);
	}
	
	/**
	 * <root>
	 * <field>
	 *   <name>
	 *   <type>
	 * </field>
	 * </root>
	 * 
	 * <root>
	 * <lookup>
	 *   <name>
	 *   <label>
	 * </lookup>
	 * </root>
	 */
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		String fieldName = (String)request.getAttributes().get("fieldName");		
		String mode = (String)request.getAttributes().get("mode");
		
		if ("field".equals(mode)) {
			Field field = getField(fieldName);
			if (field == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			
			response.setEntity(updateField(fieldName, field, entity), MediaType.TEXT_PLAIN);
		}
		else if ("lookup".equals(mode)) {
			Map<Integer, String> values = generator.loadLookup(fieldName);
			
			response.setEntity(updateLookup(fieldName, values.values(), entity), MediaType.TEXT_PLAIN);
		}
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	@Override
	public void handleDelete(Request request, Response response) throws ResourceException {
		String schema = (String)request.getAttributes().get("schema");
		String mode = (String)request.getAttributes().get("mode");
		String fieldName = (String)request.getAttributes().get("fieldName");
		
		String sql;
		if ("lookup".equals(mode)) {
			String prim;
			try {
				prim = URLDecoder.decode((String)request.getAttributes().get("prim"), "UTF-8");
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			if (prim == null)
				sql = runQuery("DROP TABLE " + fieldName);
			else
				sql = removeLookupOption(fieldName, prim);
		}
		else if ("field".equals(mode)) {
			String prim = (String)request.getAttributes().get("prim");
			
			if (prim == null)
				sql = runQuery("DROP TABLE "+ fieldName);
			else
				sql = removeField(fieldName, prim);
		}
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		
		response.setEntity(sql, MediaType.TEXT_PLAIN);
	}
	
	private String updateField(String fieldName, Field field, Representation entity) throws ResourceException {
		final StringBuilder sql = new StringBuilder();
		final Document document = getEntityAsDocument(entity);
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
		for (Node node : nodes) {
			if ("field".equals(node.getNodeName())) {
				String name = null, dataType = null;
				NodeCollection children = new NodeCollection(node.getChildNodes());
				for (Node child : children) {
					if ("name".equals(child.getNodeName()))
						name = child.getTextContent();
					else if ("type".equals(child.getNodeName()))
						dataType = child.getTextContent();
				}
				
				if (name == null || dataType == null)
					continue;
				
				String numAllowed;
				if ("field".equals(dataType))
					numAllowed = "*";
				else if (PrimitiveFieldFactory.generatePrimitiveField(dataType) != null) {
					dataType = PrimitiveFieldType.get(dataType).getMatches()[0];
					numAllowed = "?";
				}
				else
					continue;
				
				if (field.getPrimitiveField(name) == null)
					sql.append(addField(fieldName, name, dataType, numAllowed));
			}
		}
		
		return sql.toString();
	}
	
	private String updateLookup(String fieldName,Collection<String> labels, Representation entity) throws ResourceException {
		final StringBuilder sql = new StringBuilder();
		final Document document = getEntityAsDocument(entity);
		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
		int index = 0;
		for (Node node : nodes) {
			System.out.println("JParse " + node.getNodeName());
			if ("lookup".equals(node.getNodeName())) {
				String label = null;
				NodeCollection children = new NodeCollection(node.getChildNodes());
				for (Node child : children) {
					if ("label".equals(child.getNodeName()))
						label = child.getTextContent();
				}
				
				if (label == null)
					continue;
				
				if (!labels.contains(label)) {
					String name = "" + (index + labels.size());
					sql.append(addLookupOption(fieldName, name, label));
					index++;
				}
			}
		}
		
		return sql.toString();
	}
	
	private String addField(String parentField, String name, String type, String numberAllowed) throws ResourceException {
		return runQuery("INSERT INTO " + parentField + " (name, data_type, number_allowed) VALUES " + 
			"('" + name + "', '" + type + "', '" + numberAllowed + "')"
		);
	}
	
	private String removeField(String parentField, String name) throws ResourceException {
		return runQuery("DELETE FROM " + parentField + " WHERE name = '" + name + "'");
	}
	
	private String addLookupOption(String parentField, String name, String label) throws ResourceException {
		return runQuery("INSERT INTO " + parentField + " (name, label) VALUES " + 
			"('" + name + "', '" + label + "')"
		);
	}
	
	private String removeLookupOption(String parentField, String name) throws ResourceException {
		return runQuery("DELETE FROM " + parentField + " WHERE name = '" + name + "'");
	}
	
	private Field getField(String fieldName) {
		try {
			return generator.getField(fieldName);
		} catch (Exception e) {
			return null;
		}
	}
	
	private AssessmentSchema getSchema(String schemaID) throws ResourceException {
		AssessmentSchema schema = SIS.get().getAssessmentSchemaBroker().getAssessmentSchema(schemaID);
		if (schema == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "The schema " + schemaID + " could not be found.");
		
		return schema;
		
	}

	private String runQuery(String sql) throws ResourceException {
		/*if (true) {
			System.out.println(sql);
			return sql;
		}*/
		
		try {
			ec.doUpdate(sql);
		} catch (DBException e) {
			Debug.println(e);
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
		
		return sql;
	}
	
}
