package org.iucn.sis.server.restlets.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.api.fields.definitions.FieldDefinitionLoader;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.NotFoundException;

public class FieldRestlet extends BaseServiceRestlet {
	
	private final FieldSchemaGenerator generator;

	public FieldRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		
		FieldSchemaGenerator generator;
		try {
			generator = new FieldSchemaGenerator();
		} catch (NamingException e) {
			generator = null;
			e.printStackTrace();
		}
		
		this.generator = generator;
	}

	@Override
	public void definePaths() {
		paths.add("/field");
		paths.add("/field/{fieldList}");
	}

	private String getFieldAsString(String fieldName) throws IOException {
		Document document = FieldDefinitionLoader.get(fieldName);
		
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
		
			//Append lookup table info
			Map<String, Map<Integer, String>> lookups = null;;
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
			
			//Add classification scheme info
			if (document.getElementsByTagName("tree").getLength() > 0) {
				try {
					generator.appendCodesForClassificationScheme(document, fieldName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return BaseDocumentUtils.impl.serializeDocumentToString(document, true, false);
		}
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		String fieldList = (String) request.getAttributes().get("fieldList");
		
		List<String> fields;
		if (fieldList.contains(","))
			fields = Arrays.asList(fieldList.split(","));
		else {
			fields = new ArrayList<String>();
			fields.add(fieldList);
		}
	
		return getFields(fields);
	}
	
	@Override
	public void handlePost(Representation rep, Request request, Response response) throws ResourceException {
		final Document entity = getEntityAsDocument(rep);
		
		final List<String> fields = new ArrayList<String>();
		
		final NodeCollection nodes = new NodeCollection(
			entity.getDocumentElement().getChildNodes()
		);
		for (Node node : nodes) {
			if ("field".equals(node.getNodeName()))
				fields.add(node.getTextContent());
		}
		
		response.setEntity(getFields(fields));
		response.setStatus(Status.SUCCESS_OK);
	}
	
	public Representation getFields(Collection<String> fieldNames) {
		StringBuilder ret = new StringBuilder("<fields>\r\n");
		for (String field : fieldNames) {
			try {
				ret.append(getFieldAsString(field));
			} catch (NotFoundException e) {
				Debug.println("Field {0} not found!  Skipping...", field);
			} catch (IOException e) {
				Debug.println("Field {0} not added!  Skipping...", field);
			}
		}
		ret.append("</fields>");
		
		return new StringRepresentation(ret.toString(), MediaType.TEXT_XML);
	}
	
}
