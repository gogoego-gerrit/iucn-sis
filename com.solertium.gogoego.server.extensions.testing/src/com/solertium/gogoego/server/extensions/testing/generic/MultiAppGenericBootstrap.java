package com.solertium.gogoego.server.extensions.testing.generic;

import java.util.Map;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.ServerApplicationAPI;
import org.restlet.Context;
import org.restlet.routing.Router;

public abstract class MultiAppGenericBootstrap extends GenericBootstrap {
	
	public MultiAppGenericBootstrap() {
		super();
	}
	
	public MultiAppGenericBootstrap(int httpPort, int httpsPort) {
		super(httpPort, httpsPort);
	}
	
	protected abstract Map<String, GoGoEgoApplication> getGoGoEgoApplications();
	
	protected final GoGoEgoApplication getGoGoEgoApplication() {
		throw new UnsupportedOperationException("Not supported in Multi App Environment");	
	}
	
	protected final String getRegistrationKey() {
		throw new UnsupportedOperationException("Not supported in Multi App Environment");
	}
	
	Router initAndInstall(Context context, TestingBackbone backbone, ServerApplicationAPI api) {
		final Router router = new Router(context);
		
		for (Map.Entry<String, GoGoEgoApplication> entry : getGoGoEgoApplications().entrySet()) {
			final GoGoEgoApplication app = entry.getValue();
			backbone.setApplication(entry.getKey(), app);
		
			app.init(api, entry.getKey());
		
			if (app.isInstalled()) {
				router.attach(buildMount(publicRouteMount, app.getRegistrationKey()), app.getPublicRouter());
				router.attach(buildMount(privateRouteMount, app.getRegistrationKey()), app.getPrivateRouter());
			}
			else
				throw new RuntimeException("Could not install application " + entry.getKey() + ".");
		}
		
		return router;
	}

}
