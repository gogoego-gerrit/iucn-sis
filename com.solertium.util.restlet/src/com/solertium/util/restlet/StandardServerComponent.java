/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.util.restlet;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.VirtualHost;

import com.solertium.db.DBSessionFactory;
import com.solertium.util.CurrentBinary;

/**
 * This is our usual Restlet component configuration for our server side use.
 * 
 * @author rob.heittman
 */
public class StandardServerComponent extends Component {

	private static Properties initProperties = null;
	protected final Logger logger;
	private boolean sslSupported = false;
	
	private final int httpPort;
	private final int httpsPort;
	
	public int getHttpPort(){
		return httpPort;
	}
	
	public int getHttpsPort(){
		return httpPort;
	}

	public boolean isSslSupported() {
		return sslSupported;
	}
	
	@Override
	public void start() throws Exception {
		setup();
		super.start();
	}
	
	public static Properties getInitProperties(){
		return initProperties;
	}

	public StandardServerComponent() {
		this(80,443);
	}
	
	protected File getConfigFile(File workingDirectory){
		File localProps = new File(workingDirectory,"local_config.properties");
		if(localProps.exists()) return localProps;
		return new File(workingDirectory,"component_config.properties");
	}
	
	public StandardServerComponent(int defaultHttpPort, int defaultSslPort) {
		this(defaultHttpPort, defaultSslPort, null);
	}
	
	public StandardServerComponent(int defaultHttpPort, int defaultSslPort, Properties providedProperties) {
		super();
		logger = Logger.getLogger(getClass().getName());
		
		if (providedProperties == null) {
			final StandardServerComponentProperties properties = 
				new StandardServerComponentProperties() {
				public File getWorkingDirectory() {
					File file = CurrentBinary.getDirectory(StandardServerComponent.this);
					if (file.getAbsolutePath().endsWith(File.separatorChar + "bin"))
						file = file.getParentFile();
					return file;
				}
			};
			properties.refresh();
			if (properties.usesSystemProperties())
				logger.log(Level.INFO, "Configuration file not found, using system properties");
			
			initProperties = properties.getInitProperties();
		}
		else
			initProperties = providedProperties;
		
		String ihttpport = initProperties.getProperty("HTTP_PORT");
		logger.log(Level.FINEST, "HTTP_PORT=" + ihttpport);
		if (ihttpport != null)
			httpPort = Integer.parseInt(ihttpport);
		else
			httpPort = defaultHttpPort;
		
		String ihttpsport = initProperties.getProperty("HTTPS_PORT");
		
		logger.log(Level.FINEST, "HTTPS_PORT=" + ihttpsport);
		if (ihttpsport != null)
			httpsPort = Integer.parseInt(ihttpsport);
		else
			httpsPort = defaultSslPort;

		getClients().add(Protocol.FILE);
		getClients().add(Protocol.HTTP);
		getClients().add(Protocol.HTTPS);
		getClients().add(Protocol.CLAP);
		
		try{
			DBSessionFactory.registerDataSources(initProperties);
		} catch (NamingException nx) {
			throw new RuntimeException("Database initialization failed",nx);
		}
	}
	
	protected void setPorts() {
	}

	/**
	 * This creates HTTP and HTTPS servers using a passed set of Properties:
	 * <p>
	 * LISTEN (default: any) - IP address to listen to
	 * <p>
	 * HTTP_PORT (default: 80) - Port which will run GoGoEgo normal HTTP
	 * services
	 * <p>
	 * KEYSTORE (no default) - Location of the SSL keystore file
	 * <p>
	 * KEYSTORE_PASSWORD (default: changeit) - Password for the keystore file
	 * <p>
	 * HTTPS_PORT (default: 443) - Port which will run GoGoEgo HTTPS services.
	 * Only used if KEYSTORE is specified.
	 */
	protected void createServers() {
		Properties properties = getInitProperties();
		
		final String listen = properties.getProperty("LISTEN");
		logger.log(Level.FINEST, "LISTEN=" + listen);
	
		if(httpsPort!=0){
			final String ks = properties.getProperty("KEYSTORE");
			logger.log(Level.FINEST, "KEYSTORE=" + ks);
			if (ks != null) {
				String ikspw = properties.getProperty("KEYSTORE_PASSWORD");
				logger.log(Level.FINEST, "KEYSTORE_PASSWORD=" + ikspw);
				if (ikspw == null)
					ikspw = "changeit";
				final String kspw = ikspw;
	
				Server httpsServer = null;
				if (listen == null)
					httpsServer = new Server(getContext().createChildContext(), Protocol.HTTPS, httpsPort, null);
				else
					httpsServer = new Server(getContext().createChildContext(), Protocol.HTTPS, listen, httpsPort,
							null);
				httpsServer.getContext().getParameters().add("keystorePath", ks);
				httpsServer.getContext().getParameters().add("keystorePassword",
						kspw);
				httpsServer.getContext().getParameters().add("keyPassword", kspw);
				getServers().add(httpsServer);
				sslSupported = true;
			}
		}

		if(httpPort!=0){
			Server httpServer = null;
			if (listen == null)
				httpServer = new Server(getContext().createChildContext(), Protocol.HTTP, httpPort, null);
			else
				httpServer = new Server(getContext().createChildContext(), Protocol.HTTP, listen, httpPort, null);
			getServers().add(httpServer);
		}
	}

	protected List<VirtualHost> getVirtualHosts() {
		return null;
	}
	
    protected boolean isHostedMode(){
        return getContext().getParameters().getFirstValue("module")!=null;
    }
    
	protected void setup() {
		if(!isHostedMode()){
			System.out.println("Is not hosted mode");
			createServers();
		}

		setupDefaultVirtualHost();
		final List<VirtualHost> virtualHosts = getVirtualHosts();
		if (virtualHosts != null)
			getHosts().addAll(virtualHosts);
	}

	protected void setupDefaultVirtualHost() {
		getDefaultHost().attach(new Restlet(getContext()) {
			@Override
			public void handle(final Request request, final Response response) {
				response.setEntity(new StringRepresentation(request
						.getResourceRef().getHostDomain()
						+ " not configured on this server."));
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		});
	}
}
