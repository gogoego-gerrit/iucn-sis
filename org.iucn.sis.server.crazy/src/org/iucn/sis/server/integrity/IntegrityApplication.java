package org.iucn.sis.server.integrity;

import java.io.File;

import javax.naming.NamingException;

import org.iucn.sis.server.crossport.export.StructureLoader;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.routing.Router;

import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.restlet.DBProvidingApplication;
import com.solertium.db.restlet.DumpResource;
import com.solertium.db.restlet.QueryResource;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.restlet.VFSProvidingApplication;

/**
 * IntegrityApplication.java
 * 
 * Create and validate integrity of an assessment.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class IntegrityApplication extends Application implements
		VFSProvidingApplication, DBProvidingApplication {

	private final VFS vfs;
	private final ExecutionContext ec;

	public IntegrityApplication(Context context, String vfsroot) {
		super(context);
		VFS ivfs;
		try {
			ivfs = VFSFactory.getVFS(new File(vfsroot));
		} catch (NotFoundException nf) {
			throw new RuntimeException("The selected VFS " + vfsroot + " does not exist");
		}
		vfs = ivfs;

		try {
			ec = new SystemExecutionContext(DBSessionFactory.getDBSession("assess"));
			ec.setAPILevel(ExecutionContext.API_ONLY);
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
			// Analyzing first to get all the _lookup tables not defined in
			// struct document
			ec.setStructure(ec.analyzeExistingStructure());
			// Append our structure since it has necessary metadata
			ec.appendStructure(StructureLoader.loadPostgres(), false);
		} catch (NamingException e) {
			throw new RuntimeException("The database was not found", e);
		} catch (DBException e) {
			throw new RuntimeException(
					"The database structure could not be set", e);
		}
	}

	@Override
	public Restlet createRoot() {
		final Router root = new Router(getContext());

		root.attach("/struct", new Restlet(getContext()) {
			public void handle(Request request, Response response) {
				if (Method.GET.equals(request.getMethod())) {
					try {
						response.setEntity(new DomRepresentation(
								MediaType.TEXT_XML, StructureLoader
										.loadPostgres()));
						response.setStatus(Status.SUCCESS_OK);
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					}
				} else
					response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		});
		root.attach("/styles.css", new Restlet(getContext()) {
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
		});
		root.attach("/ruleset", RuleSetResource.class);
		root.attach("/ruleset/{rule}", RuleSetResource.class);

		root.attach("/dump", DumpResource.class);
		root.attach("/query", QueryResource.class);

		root.attach("/validate", MultipleValidationResource.class);
		root.attach("/validate/{rule}", ValidationResource.class);
		root.attach("/lookup", QBLookupListResource.class);

		root.attach("/test", IntegrityTestResource.class);

		return root;
	}

	public VFS getVFS() {
		return vfs;
	}

	public ExecutionContext getExecutionContext() {
		return ec;
	}

}
