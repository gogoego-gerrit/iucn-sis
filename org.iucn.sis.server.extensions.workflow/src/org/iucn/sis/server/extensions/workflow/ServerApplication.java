package org.iucn.sis.server.extensions.workflow;

import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.NamingException;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SIS;
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
		ec = SIS.get().getExecutionContext();
		
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

}
