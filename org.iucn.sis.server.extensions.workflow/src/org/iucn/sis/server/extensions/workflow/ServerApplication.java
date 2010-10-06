package org.iucn.sis.server.extensions.workflow;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.db.DBException;
import com.solertium.db.query.DeleteQuery;
import com.solertium.util.TrivialExceptionHandler;

public class ServerApplication extends SISApplication {
	
	public ServerApplication() {
		super();
	}
	
	@Override
	public void init() {
		addResource(WFListResource.class, "/workflow/set", true, true, false);
		addResource(WFNotesResource.class, "/workflow/set/{working-set}/notes", true, true, false);
		addResource(WFNotesResource.class, "/workflow/set/{working-set}/notes/{protocol}", true, true, false);
		addResource(WorkflowManagementResource.class, "/workflow/set/{working-set}/status", true, true, false);
		addResource(new Restlet(app.getContext()) {
			public void handle(Request arg0, Response arg1) {
				for (String table : new String[] { WorkflowConstants.WORKFLOW_TABLE, WorkflowConstants.WORKFLOW_NOTES_TABLE }) {
					DeleteQuery query = new DeleteQuery();
					query.setTable(table);
					
					try {
						SIS.get().getExecutionContext().doUpdate(query);
					} catch (DBException e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
			}
		}, "/workflow/clear", true, true, false);
	}

}
