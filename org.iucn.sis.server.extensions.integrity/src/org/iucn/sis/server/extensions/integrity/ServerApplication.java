package org.iucn.sis.server.extensions.integrity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.w3c.dom.Document;

import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.TrivialExceptionHandler;

public class ServerApplication extends SimpleSISApplication {
	
	/**
	 * Validation available online & offline
	 */
	public void init() {
		addResource(getStructRestlet(), "/struct", false);
		addResource(getStyleSheetRestlet(), "/styles.css", false);
		
		List<String> rulesetPaths = new ArrayList<String>();
		rulesetPaths.add("/ruleset");
		rulesetPaths.add("/ruleset/{rule}");
		
		addResource(RuleSetResource.class, rulesetPaths, false);
		
		addResource(IntegrityDumpResource.class, "/dump", false);
		addResource(IntegrityQueryResource.class, "/query", false);
		
		addResource(MultipleValidationResource.class, "/validate", false);
		addResource(ValidationResource.class, "/validate/{rule}", false);
		addResource(QBLookupListResource.class, "/lookup", false);
		
		addResource(IntegrityTestResource.class, "/test", false);
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
							ServerApplication.class.getResourceAsStream("styles.css"), 
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
	
	@Override
	public boolean isInstalled() {
		DBSession session;
		try {
			session = DBSessionFactory.getDBSession("integrity");
		} catch (NamingException e) {
			return false;
		}
		
		if (session == null)
			return false;
		
		session.setAllowedTableTypes("TABLE", "VIEW");
		session.setSchema("integrity");
		
		SystemExecutionContext ec = new SystemExecutionContext(session);
		
		try {
			Document doc = IntegrityStructureGenerator.generateViewStructure(ec);
			ec.appendStructure(doc, false);
		} catch (DBException e) {
			return false;
		}
		
		return super.isInstalled();
	}

}
