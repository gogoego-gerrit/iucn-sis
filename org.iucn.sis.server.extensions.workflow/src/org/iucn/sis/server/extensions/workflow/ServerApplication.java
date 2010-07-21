package org.iucn.sis.server.extensions.workflow;

import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.NamingException;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.StructureLoader;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.util.TrivialExceptionHandler;

public class ServerApplication extends SISApplication{
	
	protected final ArrayList<ServiceRestlet> services;
	private ExecutionContext ec;
	
	public ServerApplication() {
		super();
		services = new ArrayList<ServiceRestlet>();
		
	}
	
	@Override
	public void init() {
		
				
		try {
			ec = new SystemExecutionContext(DBSessionFactory.getDBSession("assess"));
			ec.setAPILevel(ExecutionContext.API_ONLY);
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			// Analyzing first to get all the _lookup tables not defined in
			// struct document
			ec.setStructure(ec.analyzeExistingStructure());
			// Append our structure since it has necessary metadata
			ec.appendStructure(StructureLoader.loadPostgres(), true);
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
		} catch (NamingException e) {
			throw new RuntimeException("The database was not found", e);
		} catch (DBException e) {
			throw new RuntimeException(
					"The database structure could not be set", e);
		}
		
		initServiceRoutes();
		initRoutes();		
	}
	
	protected void initServiceRoutes() {
		
		
		for (Iterator<ServiceRestlet> iter = services.iterator(); iter.hasNext();)
			addServiceToRouter(iter.next());
		
	}
	
	private void addServiceToRouter(ServiceRestlet curService) {
		addResource(curService, curService.getPaths(), true, true, false);
	}
	
	protected void initRoutes() {
		addResource(WFListResource.class, "/set", true, false, false);
		addResource(WFNotesResource.class, "/set/{working-set}/notes", true, false, false);
		addResource(WFNotesResource.class, "/set/{working-set}/notes/{protocol}", true, false, false);
		addResource(WorkflowManagementResource.class, "/set/{working-set}/status", true, false, false);
		addResource(new Restlet(app.getContext()) {
			public void handle(Request arg0, Response arg1) {
				for (String table : new String[] { WorkflowConstants.WORKFLOW_TABLE, WorkflowConstants.WORKFLOW_NOTES_TABLE }) {
					DeleteQuery query = new DeleteQuery();
					query.setTable(table);
					
					try {
						ec.doUpdate(query);
					} catch (DBException e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
			}
		}, "/clear", true, false, false);
		
		//TODO: GET COMPILED CLIENT BITS
				
		
	}
	
	public static ServerApplication getApplication(Context context) {
		return (ServerApplication)GoGoEgo.get().getApplication(context, "org.iucn.sis.server.extenions.workflow");
	}
	
	
	

}
