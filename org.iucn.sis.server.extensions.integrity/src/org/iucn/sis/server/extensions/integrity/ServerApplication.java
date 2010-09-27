package org.iucn.sis.server.extensions.integrity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;

import com.solertium.util.TrivialExceptionHandler;

public class ServerApplication extends SISApplication {
	
	
	@Override
	public void init() {
		addResource(getStructRestlet(), "/struct", true, true, false);
		addResource(getStyleSheetRestlet(), "/styles.css", true, true, false);
		
		List<String> rulesetPaths = new ArrayList<String>();
		rulesetPaths.add("/ruleset");
		rulesetPaths.add("/ruleset/{rule}");
		
		addResource(RuleSetResource.class, rulesetPaths, true, true, false);
		
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
					if ("true".equals(request.getResourceRef().getQueryAsForm().getFirstValue("fresh"))) {
						try {
							SIS.get().getVFS().delete(IntegrityStructureGenerator.CACHED_STRUCTURE);
						} catch (IOException e) {
							TrivialExceptionHandler.ignore(this, e);
						}
					}
					
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
