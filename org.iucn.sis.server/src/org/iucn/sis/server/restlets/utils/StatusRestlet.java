package org.iucn.sis.server.restlets.utils;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.locking.LockException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.User;
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
		Integer id = Integer.valueOf((String) request.getAttributes().get("id"));
		String modDate = (String) request.getAttributes().get("lastModDate");
		User user = SIS.get().getUser(request);

		String ret;
		try {
			ret = SIS.get().getLocker().checkAssessmentAvailability(id, modDate, user);
		} catch (LockException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return;
		}

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
		if (request.getResourceRef().getPath().contains("/status/assessment")) {
			try {
				checkAssessmentStatus(request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (request.getResourceRef().getPath().contains("/status/taxon")) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
}
