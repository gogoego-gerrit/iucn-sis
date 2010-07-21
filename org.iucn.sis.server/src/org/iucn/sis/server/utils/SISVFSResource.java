package org.iucn.sis.server.utils;

import org.iucn.sis.server.api.application.SIS;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.vfs.VFS;
import com.solertium.vfs.restlet.VFSResource;

public class SISVFSResource extends VFSResource {
	
	public SISVFSResource(final Context context, final Request request,
			final Response response) {
		super(context, request, response);
	}

	@Override
	protected VFS getVFS(final Context context) {
		return SIS.get().getVFS();
	}
}
