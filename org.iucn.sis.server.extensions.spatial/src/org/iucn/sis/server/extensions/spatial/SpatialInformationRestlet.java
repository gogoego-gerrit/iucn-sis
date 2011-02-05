package org.iucn.sis.server.extensions.spatial;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class SpatialInformationRestlet extends BaseServiceRestlet {
	
	private final VFS vfs;

	public SpatialInformationRestlet(Context context) {
		super(context);
		vfs = SIS.get().getVFS();
	}

	@Override
	public void definePaths() {
		paths.add("/spatial/{format}/{taxonID}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		String taxonID = (String) request.getAttributes().get("taxonID");
		String format = (String) request.getAttributes().get("format");
	
		final VFSPath uri = new VFSPath("/browse/spatial/" + taxonID + "." + format);
		if (vfs.exists(uri)) {
			MediaType mt = null;
			if (format.equalsIgnoreCase("jpg"))
				mt = MediaType.IMAGE_JPEG;
			else if (format.equalsIgnoreCase("kml"))
				mt = MediaType.TEXT_XML;
			else
				mt = MediaType.ALL;

			try {
				return new InputRepresentation(vfs.getInputStream(uri), mt);
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		} else
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
	}

}
