package org.iucn.sis.server.simple;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class NewsReslet extends ServiceRestlet {
	public NewsReslet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/news/{lastLoggedIn}");
	}

	@Override
	public void performService(Request request, Response response) {
		// String lastLoggedIn =
		// (String)request.getAttributes().get("lastLoggedIn");

	}
}
