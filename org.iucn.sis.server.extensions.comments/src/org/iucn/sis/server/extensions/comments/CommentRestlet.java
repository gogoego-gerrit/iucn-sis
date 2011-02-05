package org.iucn.sis.server.extensions.comments;

import java.io.Writer;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.vfs.VFS;

public class CommentRestlet extends BaseServiceRestlet {
	
	private final VFS vfs;

	public CommentRestlet(Context context) {
		super(context);
		vfs = SIS.get().getVFS();
	}

	@Override
	public void definePaths() {
		paths.add("/comments/{assessmentID}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		String assessmentID = (String)request.getAttributes().get("assessmentID");
		String url = null;

		if (vfs.exists("/comments/" + assessmentID + ".xml"))
			url = "/comments/" + assessmentID + ".xml";
		else if (vfs.exists("/comments/" + assessmentID))
			url = "/comments/" + assessmentID;

		if (url == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		Representation rep = new DomRepresentation(MediaType.TEXT_XML, 
			DocumentUtils.getVFSFileAsDocument(url, vfs));
		rep.setMediaType(MediaType.TEXT_XML);

		return rep;
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		handlePut(entity, request, response, session);
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		String assessmentID = (String)request.getAttributes().get("assessmentID");
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

}
