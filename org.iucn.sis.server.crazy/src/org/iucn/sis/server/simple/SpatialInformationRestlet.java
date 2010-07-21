package org.iucn.sis.server.simple;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;

import com.solertium.util.SysDebugger;

public class SpatialInformationRestlet extends ServiceRestlet {

	public SpatialInformationRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/spatial/{format}/{taxonID}");
	}

	private void fetchSpatialData(String taxonID, String format, Response response) {
		SysDebugger.getInstance().println("Looking for: " + "/browse/spatial/" + taxonID + "." + format);
		if (vfs.exists("/browse/spatial/" + taxonID + "." + format)) {
			MediaType mt = null;
			if (format.equalsIgnoreCase("jpg"))
				mt = MediaType.IMAGE_JPEG;
			else if (format.equalsIgnoreCase("kml"))
				mt = MediaType.TEXT_XML;
			else
				mt = MediaType.ALL;

			try {
				InputRepresentation ir = new InputRepresentation(vfs.getInputStream("/browse/spatial/" + taxonID + "."
						+ format), mt);
				response.setEntity(ir);
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				e.printStackTrace();
				SysDebugger.getInstance().println(
						"Error reading in spatial information from " + "/browse/spatial/" + taxonID + "." + format);
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
	}

	@Override
	public void performService(Request request, Response response) {
		String id = (String) request.getAttributes().get("taxonID");
		String format = (String) request.getAttributes().get("format");

		if (id != null && !id.equalsIgnoreCase("")) {
			fetchSpatialData(id, format, response);
		} else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}
}
