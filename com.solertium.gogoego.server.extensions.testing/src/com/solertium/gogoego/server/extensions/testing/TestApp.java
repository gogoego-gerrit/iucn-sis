package com.solertium.gogoego.server.extensions.testing;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.HasPluginUI;
import org.gogoego.api.applications.HasSettingsUI;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;


public class TestApp extends GoGoEgoApplication implements HasPluginUI, HasSettingsUI {

	public boolean isInstalled() {
		return true;
	}
	
	@Override
	public Restlet getPrivateRouter() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Restlet getPublicRouter() {
		return new Restlet(app.getContext()) {
			public void handle(Request arg0, Response arg1) {
				arg1.setEntity(new GoGoEgoStringRepresentation("You found me!!"));
			}
		};
	}
	
	public String getIconStyle() {
		return null;
	}
	
	public String getTabURL() {
		return getPath() + "/client";
	}
	
	public String getSettingsURL() {
		return getPath() + "/settings";
	}

}
