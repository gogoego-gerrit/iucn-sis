package org.iucn.sis.server.simple;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class AssessmentReviewRestlet extends ServiceRestlet {
	public AssessmentReviewRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/assessmentReview/{username}");
	}

	private void getAssessmentReviews(Request request, Response response) {
		String url = "/users/" + request.getAttributes().get("username") + "/assessmentsToReview.xml";
		String xml = DocumentUtils.getVFSFileAsString(url, vfs);
		if (xml != null) {
			response.setEntity(xml, MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getMethod().equals(Method.GET)) {
			getAssessmentReviews(request, response);
		} else
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

	}
}
