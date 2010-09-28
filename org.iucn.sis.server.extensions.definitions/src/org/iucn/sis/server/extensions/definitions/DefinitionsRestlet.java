package org.iucn.sis.server.extensions.definitions;

import java.io.IOException;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;

public class DefinitionsRestlet extends ServiceRestlet {
	
	private static final VFSPath DEFINITIONS_LOCATION = 
		new VFSPath("/browse/docs/definitions.xml");
	
	private Document definitions;
	private long lastModded = -1;

	public DefinitionsRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/definitions");
	}

	private void getDefinitions() throws NotFoundException {
		if (vfs.getLastModified(DEFINITIONS_LOCATION) == lastModded)
			return;

		try {
			definitions = vfs.getDocument(DEFINITIONS_LOCATION);
			lastModded = vfs.getLastModified(DEFINITIONS_LOCATION);
		} catch (IOException e) {
			throw new NotFoundException();
		}	
	}
	
	protected void postDefinitions(Request request) throws IOException {
		Document doc = new DomRepresentation(request.getEntity()).getDocument();
		
		if (DocumentUtils.writeVFSFile(DEFINITIONS_LOCATION.toString(), vfs, doc))
		{
			definitions = doc;
			lastModded = vfs.getLastModified(DEFINITIONS_LOCATION);
		}
		else
			throw new ConflictException("Unable to save file");		
		
	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getMethod().equals(Method.GET)) {
			try {
				getDefinitions();

				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, definitions));
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}

		} 
		else if (request.getMethod().equals(Method.POST)) {
			try {
				postDefinitions(request);
				response.setStatus(Status.SUCCESS_OK);
			} catch (IOException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}			
		}
		else
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
}
