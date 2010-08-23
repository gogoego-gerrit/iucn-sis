package com.solertium.gogoego.server.extensions.testing.generic;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.ServerHelper;
import org.restlet.ext.jetty.HttpServerHelper;

public class JettyEngine extends Engine {
	
	public JettyEngine() {
		super();
	}
	
	@Override
	public ServerHelper createHelper(Server server, String helperClass) {
		if (server.getProtocols().contains(Protocol.HTTPS))
			return super.createHelper(server, helperClass);
		else
			return new HttpServerHelper(server);
	}

}
