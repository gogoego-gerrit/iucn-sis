package org.iucn.sis.server.extensions.workflow;

import org.iucn.sis.server.api.application.SIS;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.db.ExecutionContext;
import com.solertium.db.restlet.DBResource;

public class WFDBResource extends DBResource {

	public WFDBResource(Context context, Request request, Response response) {
		super(context, request, response);
	}
	
	@Override
	protected ExecutionContext getExecutionContext() {
		return SIS.get().getExecutionContext();
	}

}
