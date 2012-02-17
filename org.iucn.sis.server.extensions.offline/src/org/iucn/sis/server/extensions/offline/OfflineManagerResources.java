package org.iucn.sis.server.extensions.offline;

import org.iucn.sis.server.extensions.offline.manager.Resources;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;

public class OfflineManagerResources extends Restlet {
	
	public OfflineManagerResources(Context context) {
		super(context);
	}
	
	@Override
	public void handle(Request arg0, Response arg1) {
		if (!Method.GET.equals(arg0.getMethod()))
			return;
		
		String file = arg0.getResourceRef().getLastSegment();
		
		try {
			arg1.setEntity(new InputRepresentation(Resources.get(file)));
			arg1.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			arg1.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

}
