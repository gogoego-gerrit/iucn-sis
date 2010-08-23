package org.iucn.sis.server.extensions.workflow;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.db.query.SelectQuery;
import com.solertium.db.restlet.DBResource;

/**
 * Access workflow data.  Normally, writing would also be available through 
 * the same resource, but for performance gains based on anticipated usage, 
 * all writing will go through one resource, so multiple tables can be updated 
 * in one request.
 * 
 * @author user
 *
 */
public class WFListResource extends DBResource {
	
	public WFListResource(Context context, Request request, Response response) {
		super(context, request, response);
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(WorkflowConstants.WORKFLOW_TABLE, "*");
		
		//TODO: find "friendly" display info for identifiers
		
		return getRowsAsRepresentation(query);
	}
	
}
