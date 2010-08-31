package org.iucn.sis.server.restlets.utils;

import java.io.IOException;
import java.util.Map;

import javax.naming.NamingException;

import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;

public class FieldRestlet extends ServiceRestlet {
	// private String fullMasterList = "";
	
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
		paths.add("/field/{fieldList}");
	}

	private String getFieldAsString(String fieldName) throws IOException {
		VFSPath path = new VFSPath("/browse/docs/fields/" + fieldName);
		if (!vfs.exists(path))
			path = new VFSPath("/browse/docs/fields/" + fieldName + ".xml");
		if (!vfs.exists(path))
			throw new NotFoundException();
		
		//final String xml = vfs.getString(path).replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
				
		if (generator == null)
			return vfs.getString(path).replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
		else {
			final Document document = vfs.getMutableDocument(path);
			
			if (generator != null) {
				/*try {
					content.append(generator.getField(fieldName).toXML("definition"));
				} catch (Exception e) {
					e.printStackTrace();
					TrivialExceptionHandler.ignore(this, e);
				}*/
			
				Map<String, Map<Integer, String>> lookups = null;;
				try {
					lookups = generator.scanForLookups(fieldName);
				} catch (Exception e) {
					e.printStackTrace();
					TrivialExceptionHandler.ignore(this, e);
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
				
				if (document.getElementsByTagName("tree").getLength() > 0) {
					try {
						generator.appendCodesForClassificationScheme(document, fieldName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			return BaseDocumentUtils.impl.serializeDocumentToString(document, true, false);
		}
	}

	@Override
	public void performService(Request request, Response response) {
		String fieldList = (String) request.getAttributes().get("fieldList");
		String[] fields = null;
		
		StringBuilder ret = new StringBuilder("<fields>\r\n");

		if (fieldList.contains(","))
			fields = fieldList.split(",");
		else
			fields = new String[] { fieldList };

		for (int i = 0; i < fields.length; i++) {
			try {
				ret.append(getFieldAsString(fields[i]));
			} catch (NotFoundException e) {
				System.out.println("Field " + fields[i] + " not found!  Skipping...");
			} catch (IOException e) {
				System.out.println("Field " + fields[i] + " not added!  Skipping...");
				e.printStackTrace();
			}
		}

		ret.append("</fields>");

		response.setEntity(ret.toString(), MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_OK);
	}
}
