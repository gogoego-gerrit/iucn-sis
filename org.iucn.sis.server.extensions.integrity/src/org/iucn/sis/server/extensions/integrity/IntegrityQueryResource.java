package org.iucn.sis.server.extensions.integrity;

import org.iucn.sis.server.api.application.SIS;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.db.ExecutionContext;
import com.solertium.db.restlet.QueryResource;

public class IntegrityQueryResource extends QueryResource {

	public IntegrityQueryResource(Context context, Request request,
			Response response) {
		super(context, request, response);
	}
	
	@Override
	protected ExecutionContext getExecutionContext() {
		return SIS.get().getLookupDatabase();
	}

}
