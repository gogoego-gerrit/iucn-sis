package org.iucn.sis.server.simple;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.locking.FileLocker;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.vfs.VFS;

public class StatusRestlet extends ServiceRestlet {

	public StatusRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	public StatusRestlet(VFS vfs, Context context) {
		super(vfs, context);
	}

	private void checkAssessmentStatus(Request request, Response response) {
		String id = (String) request.getAttributes().get("id");
		String type = (String) request.getAttributes().get("type");
		String modDate = (String) request.getAttributes().get("lastModDate");
		String username = request.getChallengeResponse().getIdentifier();

		String ret = FileLocker.impl.checkAssessmentAvailability(id, type, modDate, username, vfs);

		if (ret != null) {
			response.setEntity(ret, MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	@Override
	public void definePaths() {
		paths.add("/status/assessment/{id}/{type}/{lastModDate}");
		paths.add("/status/taxon/{id}/{lastModDate}");
	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getResourceRef().getPath().startsWith("/status/assessment")) {
			try {
				checkAssessmentStatus(request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (request.getResourceRef().getPath().startsWith("/status/taxon")) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
}
