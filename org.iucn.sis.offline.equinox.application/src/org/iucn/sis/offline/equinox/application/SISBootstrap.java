package org.iucn.sis.offline.equinox.application;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.ServerHelper;
import org.restlet.ext.jetty.HttpServerHelper;
import org.restlet.ext.jetty.HttpsServerHelper;

import com.solertium.db.DBException;
import com.solertium.gogoego.server.Bootstrap;

public class SISBootstrap extends Bootstrap {
	
	public SISBootstrap() throws DBException {
		this(11001, 11002);
	}

	public SISBootstrap(final int defaultHttpPort, final int defaultSslPort) throws DBException {
		super(defaultHttpPort, defaultSslPort);
	}
	
	protected void createServers() {
		Engine.setInstance(new JettyEngine());
		super.createServers();
	}

	private static class JettyEngine extends Engine {
		
		public JettyEngine() {
			super();
		}
		
		@Override
		public ServerHelper createHelper(Server server, String helperClass) {
			if (server.getProtocols().contains(Protocol.HTTPS))
				return new HttpsServerHelper(server);
			else
				return new HttpServerHelper(server);
		}

	}
	
}
