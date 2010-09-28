package org.iucn.sis.server.extensions.comments;

import java.io.Writer;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;

public class CommentRestlet extends ServiceRestlet {

	public CommentRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/comments/{assessmentID}");
	}

	private void doGet(Response response, String assessmentID) {
		String url = null;

		if (vfs.exists("/comments/" + assessmentID + ".xml"))
			url = "/comments/" + assessmentID + ".xml";
		else if (vfs.exists("/comments/" + assessmentID))
			url = "/comments/" + assessmentID;

		if (url == null)
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		else {
			try {
				Representation rep = new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.getVFSFileAsDocument(url,
						vfs));
				rep.setMediaType(MediaType.TEXT_XML);

				response.setEntity(rep);
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}
	}

	private void doPost(Request request, Response response, String assessmentID) {
		String url = null;

		if (vfs.exists("/comments/" + assessmentID + ".xml"))
			url = "/comments/" + assessmentID + ".xml";
		else if (vfs.exists("/comments/" + assessmentID))
			url = "/comments/" + assessmentID;

		if (url == null) {
			try {
				String newComment = request.getEntity().getText();
				String comments = "<comments>\r\n" + newComment + "</comments>\r\n";

				if (assessmentID.endsWith(".xml"))
					url = "/comments/" + assessmentID;
				else
					url = "/comments/" + assessmentID + ".xml";

				Writer writer = vfs.getWriter(url);
				writer.write(comments);
				writer.close();

				response.setEntity(comments, MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_ACCEPTED);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else {
			try {
				Representation rep = new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.getVFSFileAsDocument(url,
						vfs));
				String comments = rep.getText();
				String newComment = request.getEntity().getText();

				comments = comments.replace("</comments>", newComment + "\r\n</comments>");

				Writer writer = vfs.getWriter(url);
				writer.write(comments);
				writer.close();

				response.setEntity(comments, MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_ACCEPTED);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}

		}
	}

	@Override
	public void performService(Request request, Response response) {
		String assessmentID = (String) request.getAttributes().get("assessmentID");

		if (assessmentID == null)
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else if (request.getMethod().equals(Method.GET))
			doGet(response, assessmentID);
		else if (request.getMethod().equals(Method.POST) || request.getMethod().equals(Method.PUT))
			doPost(request, response, assessmentID);
		else
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

}
