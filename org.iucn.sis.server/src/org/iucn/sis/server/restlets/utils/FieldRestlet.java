package org.iucn.sis.server.restlets.utils;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class FieldRestlet extends ServiceRestlet {
	// private String fullMasterList = "";

	public FieldRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/field/{fieldList}");
	}

	private String getFieldAsString(String fieldName) {
		String url = null;

		try {
			if (vfs.exists("/browse/docs/fields/" + fieldName))
				url = "/browse/docs/fields/" + fieldName;
			else if (vfs.exists("/browse/docs/fields/" + fieldName + ".xml"))
				url = "/browse/docs/fields/" + fieldName + ".xml";

			String field = DocumentUtils.getVFSFileAsString(url, vfs);
			field = field.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");

			return field;
		} catch (Exception e) {
			System.out.println("Could not find " + fieldName + " at url " + url);
			return null;
		}
	}

	@Override
	public void performService(Request request, Response response) {
		String fieldList = (String) request.getAttributes().get("fieldList");
		String[] fields = null;
		StringBuffer ret = new StringBuffer("<fields>\r\n");

		if (fieldList.contains(","))
			fields = fieldList.split(",");
		else
			fields = new String[] { fieldList };

		for (int i = 0; i < fields.length; i++) {
			String content = getFieldAsString(fields[i]);
			if (content != null) {
				ret.append(content);
			}
		}

		ret.append("</fields>");

		response.setEntity(ret.toString(), MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_OK);
	}
}
