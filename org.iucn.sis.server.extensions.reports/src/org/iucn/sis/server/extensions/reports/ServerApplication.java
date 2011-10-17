package org.iucn.sis.server.extensions.reports;

import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * Reports online & offline
	 */
	public void init() {
		addServiceToRouter(new AssessmentReportRestlet(app.getContext()));
		addResource(getStyleSheetRestlet(), "/css/reportStyles.css", false);
		addResource(getImagesRestlet(), "/images/iucnLogo.gif", false);
	}
	
	private Restlet getStyleSheetRestlet() {
		return new Restlet(app.getContext()) {
			public void handle(Request request, Response response) {
				if (Method.GET.equals(request.getMethod())) {
					try {
						response.setEntity(new InputRepresentation(
								AssessmentReportRestlet.class.getResourceAsStream("reportStyles.css"), 
							MediaType.TEXT_CSS
						));
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
				else
					response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		};
	}
	
	private Restlet getImagesRestlet() {
		return new Restlet(app.getContext()) {
			public void handle(Request request, Response response) {
				if (Method.GET.equals(request.getMethod())) {
					try {
						response.setEntity(new InputRepresentation(
								AssessmentReportRestlet.class.getResourceAsStream("iucnLogo.gif"), 
							MediaType.IMAGE_GIF
						));
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
				else
					response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		};
	}	
}
