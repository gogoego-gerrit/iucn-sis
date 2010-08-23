package org.iucn.sis.server.extensions.workflow;

import java.io.File;

import javax.naming.NamingException;

import org.iucn.sis.server.api.utils.StructureLoader;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Router;

import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.restlet.DBProvidingApplication;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.restlet.VFSProvidingApplication;

public class WorkflowApplication extends Application implements 
		VFSProvidingApplication, DBProvidingApplication {
	
	private final VFS vfs;
	private final ExecutionContext ec;
	
	public WorkflowApplication(Context context, String vfsroot) {
		super(context);
		VFS ivfs;
		try {
			ivfs = VFSFactory.getVFS(new File(vfsroot));
		} catch (NotFoundException nf) {
			ivfs = null;
			//throw new RuntimeException("The selected VFS " + vfsroot + " does not exist");
		}
		vfs = ivfs;
		
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
	}
	
	public Restlet createRoot() {
		final Router root = new Router(getContext());
		
		root.attach("/set", WFListResource.class);
		
		root.attach("/set/{working-set}/notes", WFNotesResource.class);
		root.attach("/set/{working-set}/notes/{protocol}", WFNotesResource.class);
		
		root.attach("/set/{working-set}/status", WorkflowManagementResource.class);
		
		root.attach("/clear", new Restlet(getContext()) {
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
		});
		
		return root;
	}
	
	public VFS getVFS() {
		return vfs;
	}

	public ExecutionContext getExecutionContext() {
		return ec;
	}

}
