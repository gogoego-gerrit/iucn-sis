package org.iucn.sis.server.extensions.integrity;

import org.iucn.sis.server.api.application.SISApplication;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;

import com.solertium.db.restlet.DumpResource;
import com.solertium.db.restlet.QueryResource;

public class ServerApplication extends SISApplication {
	
	
	@Override
	public void init() {
		addResource(getStructRestlet(), "/struct", true, true, false);
		addResource(getStyleSheetRestlet(), "/styles.css", true, true, false);
		
		addResource(RuleSetResource.class, "/ruleset", true, true, false);
		addResource(RuleSetResource.class, "/ruleset/{rule}", true, true, false);
		
		addResource(IntegrityDumpResource.class, "/dump", true, true, false);
		addResource(IntegrityQueryResource.class, "/query", true, true, false);
		
		addResource(MultipleValidationResource.class, "/validate", true, true, false);
		addResource(ValidationResource.class, "/validate/{rule}", true, true, false);
		addResource(QBLookupListResource.class, "/lookup", true, true, false);
		
		addResource(IntegrityTestResource.class, "/test", true, true, false);
	}
	
	private Restlet getStructRestlet() {
		return new Restlet(app.getContext()) {
			public void handle(Request request, Response response) {
				if (Method.GET.equals(request.getMethod())) {
					try {
						response.setEntity(new DomRepresentation(
								MediaType.TEXT_XML, IntegrityStructureGenerator.generate()));
						response.setStatus(Status.SUCCESS_OK);
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					}
				} else
					response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		};
	}
	
	private Restlet getStyleSheetRestlet() {
		return new Restlet(app.getContext()) {
			public void handle(Request request, Response response) {
				if (Method.GET.equals(request.getMethod())) {
					try {
						response.setEntity(new InputRepresentation(
							IntegrityApplication.class.getResourceAsStream("styles.css"), 
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
	

}
