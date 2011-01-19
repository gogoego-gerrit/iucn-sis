package org.iucn.sis.server.extensions.tags;

import java.io.IOException;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.vfs.VFS;

public class MarkedRestlet extends BaseServiceRestlet {

	private final VFS vfs;
	
	/**
	 * FIXME: Push this to the database...
	 * @param context
	 */
	public MarkedRestlet(Context context) {
		super(context);
		vfs = SIS.get().getVFS();
	}

	@Override
	public void definePaths() {
		paths.add("/mark/{username}");
	}
	
	public String getUsersFolder(String username) {
		return ServerPaths.getUserPath(username);
	}

	public String getUsersMarked(String username) {
		return getUsersFolder(username) +  "/marked.xml";
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		String username = (String) request.getAttributes().get("username");
		
		if (!vfs.exists(getUsersFolder(username)))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		
		if (!vfs.exists(getUsersMarked(username))) {
			//USER HAS NOT YET MARKED ANYTHING
			
			return new StringRepresentation("<marked></marked>", MediaType.TEXT_XML);
		} else
			try {
				return new InputRepresentation(vfs.getInputStream(getUsersMarked(username)), MediaType.TEXT_XML);
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
		String username = (String) request.getAttributes().get("username");
		if (!vfs.exists(getUsersFolder(username)))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		
		Document doc = getEntityAsDocument(entity);
		if (doc.getDocumentElement().getTagName().equalsIgnoreCase("marked")) {
			DocumentUtils.writeVFSFile(getUsersMarked(username), vfs, doc);
			response.setStatus(Status.SUCCESS_CREATED);
		} else
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
	}

}
