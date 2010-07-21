package org.iucn.sis.server.extensions.tags;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;

public class MarkedRestlet extends ServiceRestlet {

	public MarkedRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/mark/{username}");
	}

	private void getMarked(Response response, String username) {

		if (!vfs.exists(getUsersFolder(username))) {
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}

		else if (!vfs.exists(getUsersMarked(username))) {
			//USER HAS NOT YET MARKED ANYTHING
			response.setEntity("<marked></marked>", MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);			
		} else {
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.getVFSFileAsDocument(
					getUsersMarked(username), vfs)));
			response.setStatus(Status.SUCCESS_OK);
		}
	}

	public String getUsersFolder(String username) {
		return ServerPaths.getUserPath(username);
	}

	public String getUsersMarked(String username) {
		return getUsersFolder(username) +  "/marked.xml";
	}

	@Override
	public void performService(Request request, Response response) {

		String username = (String) request.getAttributes().get("username");

		if (request.getMethod().equals(Method.GET)) {
			getMarked(response, username);
		} else if (request.getMethod().equals(Method.PUT)) {
			putMarked(request, response, username);
		} else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}

	private void putMarked(Request request, Response response, String username) {

		if (!vfs.exists(getUsersFolder(username))) {
			System.out.println("the user does not exist");
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		} else {
			try {
				Document doc = new DomRepresentation(request.getEntity()).getDocument();
				if (doc.getDocumentElement().getTagName().equalsIgnoreCase("marked")) {
					DocumentUtils.writeVFSFile(getUsersMarked(username), vfs, doc);
					response.setStatus(Status.SUCCESS_CREATED);
				} else
					response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}
	}

}
