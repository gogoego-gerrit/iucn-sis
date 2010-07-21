package com.solertium.gogoego.server.lib.app.publishing.worker;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.gogoego.server.lib.app.publishing.container.CollectionPublishingSettings;
import com.solertium.gogoego.server.lib.app.publishing.container.PublishingApplication;

public class OnPublishNotifier implements Runnable {
	
	private final CollectionPublishingSettings settings;
	private final Context context;
	
	public OnPublishNotifier(CollectionPublishingSettings settings, Context context) {
		this.settings = settings;
		this.context = context;
	}
	
	public void run() {
		final String key = settings.getSetting("key", "changeme");
			
		final String list = settings.getSetting("notify");
		if (list == null || "".equals(list))
			return;
		
		Client c = null;  
		
		for (String url : list.split("\n")) {
			String fullUri = "";
			if (!(url.startsWith("http:") || url.startsWith("https:")))
				fullUri += "http://";
			fullUri += url;
			
			fullUri += "/apps/"+PublishingApplication.REGISTRATION+"/import?key=" + key;
			
			final Request request = new Request(Method.GET, fullUri);
			if (c == null || !c.getProtocols().contains(request.getProtocol()))
				c = new Client(request.getProtocol());
			
			final Response response = c.handle(request);
			
			GoGoEgo.debug().println("Collection Publishing notified " + url + ": " + response.getStatus());
		}
	}

}
