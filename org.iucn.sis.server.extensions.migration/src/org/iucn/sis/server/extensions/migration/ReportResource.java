package org.iucn.sis.server.extensions.migration;

import java.io.IOException;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.TransactionResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

@SuppressWarnings("deprecation")
public class ReportResource extends TransactionResource {

	public ReportResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);
		
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
	
	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		VFSPath uri = new VFSPath("/migration" + getRequest().getResourceRef().getRemainingPart());
		
		VFS vfs = SIS.get().getVFS();
		if (!vfs.exists(uri))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		try {
			return new InputRepresentation(vfs.getInputStream(uri), variant.getMediaType());
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public void removeRepresentations(Session session) throws ResourceException {
		VFSPath uri = new VFSPath("/migration" + getRequest().getResourceRef().getRemainingPart());
		
		VFS vfs = SIS.get().getVFS();
		if (!vfs.exists(uri))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		try {
			vfs.delete(uri);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
}
