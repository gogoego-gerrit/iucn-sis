package org.iucn.sis.server.simple;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class CitationsRestlet extends ServiceRestlet {

	public CitationsRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/citation/{referenceID}");
	}

	private void doGet(Request request, Response response, String refID) {

	}

	private void doPost(Request request, Response response, String refID) {

	}

	private void doPut(Request request, Response response, String refID) {

	}

	@Override
	public void performService(Request request, Response response) {

		String referenceID = (String) request.getAttributes().get("referenceID");

		if (referenceID == null)
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else if (request.getMethod().equals(Method.GET))
			doGet(request, response, referenceID);
		else if (request.getMethod().equals(Method.PUT))
			doPut(request, response, referenceID);
		else if (request.getMethod().equals(Method.POST))
			doPost(request, response, referenceID);
		else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}

}
