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

package com.solertium.gogoego.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.authentication.AuthenticatorFactory;
import org.gogoego.api.authentication.CookieAuthenticatorFactory;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.ServiceReference;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.VirtualHost;

import com.solertium.db.DBException;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.DesktopIntegration;
import com.solertium.util.restlet.StandardServerComponent;
import com.solertium.util.restlet.DesktopIntegration.IconProvider;

/**
 * Bootstrap is the entry point for the standalone GoGoEgo container. It is the
 * Main-Class for the single JAR file distribution. Behavior is controlled by
 * system properties passed in to the executing virtual machine.
 * <p>
 * COMPONENT_CONFIG (default: instance.ini) - Instead of specifying each configuration
 * property separately, the name of a properties file can be passed here; this
 * properties file will be used instead of system properties.
 * <p>
 * GOGOEGO_VMROOT (default: workspace) - Root for this GoGoEgo installation.
 * It should contain a hosts.xml file with further information on how to
 * configure virtual hosts.
 * <p>
 * 
 * @see GoGoEgoVirtualHost
 * @see ServerApplication
 * @see com.solertium.container.TypicalServerComponent for a list of supported
 *      port and path properties.
 * 
 * @author Rob Heittman <rob.heittman@solertium.com>
 * 
 */
public class Bootstrap extends StandardServerComponent {

	Restarter restarter;

	public static void main(final String args[]) {
		Bootstrap component;
		try {
			component = new Bootstrap();
		} catch (DBException e) {
			System.err.println("Could not start DBSession: " + e.getMessage());
			return;
		}

		if ((args.length > 0) && "-desktop".equals(args[args.length - 1]))
			DesktopIntegration.launch("GoGoEgo", "/admin/index.html", component.getIconProvider(), component);
		else
			try {
				component.start();
			} catch (final Exception startupException) {
				startupException.printStackTrace();
			}
	}

	public Bootstrap() throws DBException {
		this(11001, 11002);
	}

	public Bootstrap(final int defaultHttpPort, final int defaultSslPort) throws DBException {
		super(defaultHttpPort, defaultSslPort, GoGoEgo.getInitProperties());
	}

	@Override
	protected void setup() {
		System.out.println("Initializing ImageIO");
		try{
			ImageIO.read(Bootstrap.class.getResourceAsStream("init.gif"));
		} catch (IOException iox) {
			iox.printStackTrace();
		}	
		System.out.println("Done initializing ImageIO");
		
		//Turn off global logging; each app uses its own logger.
		getLogService().setEnabled(false);
		
		System.out.println("Starting Seppuku monitoring for bootstrap "+this.toString()+" in thread "+Thread.currentThread().getName()+":"+Thread.currentThread().getId());
		new Thread(new Seppuku(this,getInitProperties()),"Seppuku").start();

		GoGoDebug.system().println("Bootstrap setup was invoked");
		super.setup();
		
		PluginAgent.getGoGoEgoApplicationBroker().addListener(new PluginBroker.PluginListener<GoGoEgoApplicationFactory>() {
			public void onRegistered(GoGoEgoApplicationFactory service, ServiceReference reference) {
				for (VirtualHost host : getHosts()) {
					if (host instanceof GoGoEgoVirtualHost) {
						((GoGoEgoVirtualHost) host).reloadApplication(reference.getBundle().getSymbolicName());
					}
				}
			}
			public void onModified(GoGoEgoApplicationFactory service, ServiceReference reference) {
				onRegistered(service, reference);
			}
			public void onUnregistered(String symbolicName) {
				for (VirtualHost host : getHosts())
					if (host instanceof GoGoEgoVirtualHost)
						((GoGoEgoVirtualHost) host).removeApplication(symbolicName);
				
				PluginAgent.getGoGoEgoApplicationBroker().release(symbolicName);
			}
		});
		PluginAgent.getAuthenticatorBroker().addListener(new PluginBroker.PluginListener<AuthenticatorFactory>() {
			public void onModified(AuthenticatorFactory service, ServiceReference reference) {
				onRegistered(service, reference);
			}
			public void onRegistered(AuthenticatorFactory service, ServiceReference reference) {
				try {
					for (VirtualHost host : getHosts()) {
						if (host instanceof GoGoEgoVirtualHost) {
							((GoGoEgoVirtualHost) host).reloadAuthenticator(
								reference.getBundle().getSymbolicName(), service 
								//PluginAgent.getAuthenticatorBroker().getPlugin(reference.getBundle().getSymbolicName())	
							);
						}
					}
				} catch (Throwable e) { //May not be started yet
					TrivialExceptionHandler.ignore(this, e);
				}
			}
			public void onUnregistered(String symbolicName) {
				for (VirtualHost host : getHosts())
					if (host instanceof GoGoEgoVirtualHost)
						((GoGoEgoVirtualHost) host).removeAuthenticator(symbolicName);
				
				PluginAgent.getAuthenticatorBroker().release(symbolicName);
			}
		});
		PluginAgent.getCookieAuthenticatorBroker().addListener(new PluginBroker.PluginListener<CookieAuthenticatorFactory>() {
			public void onModified(CookieAuthenticatorFactory service, ServiceReference reference) {
				onRegistered(service, reference);
			}
			public void onRegistered(CookieAuthenticatorFactory service, ServiceReference reference) {
				for (VirtualHost host : getHosts()) {
					if (host instanceof GoGoEgoVirtualHost) {
						((GoGoEgoVirtualHost) host).reloadCookieAuthenticator(
							reference.getBundle().getSymbolicName(),
							service
							//PluginAgent.getCookieAuthenticatorBroker().getPlugin(reference.getBundle().getSymbolicName())	
						);
					}
				}
			}
			public void onUnregistered(String symbolicName) {
				for (VirtualHost host : getHosts())
					if (host instanceof GoGoEgoVirtualHost)
						((GoGoEgoVirtualHost) host).removeCookieAuthenticator(symbolicName);
				
				PluginAgent.getCookieAuthenticatorBroker().release(symbolicName);
			}
		});
	}

	@Override
	protected void setupDefaultVirtualHost() {
		final Context childContext = getContext().createChildContext();

		MultiHostConfigurator mhc = new MultiHostConfigurator();
		mhc.configure(this, childContext, GoGoEgo.getInitProperties().getProperty("GOGOEGO_VMROOT"));

		getDefaultHost().attach(new Restlet(childContext) {
			@Override
			public void handle(final Request request, final Response response) {
				GoGoDebug.get("force").println("Default host caused 404");
				response.setEntity(new StringRepresentation(request.getResourceRef().getHostDomain()
						+ " not found on this installation."));
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		});
	}
	
	public final static int UNSTARTED = -1;
	public final static int STARTING = 0;
	public final static int STARTED = 1;
	AtomicInteger started = new AtomicInteger(UNSTARTED);
	
	public void start() throws Exception {
		if(started.compareAndSet(UNSTARTED, STARTING)){
			System.out.println("Bootstrap "+toString()+" on "+Thread.currentThread().getName()+":"+Thread.currentThread().getId()+" is starting the component.");
		} else {
			// this thread is not the one starting the component.  Wait for another to do so
			int waits = 0;
			while(started.get()!=STARTED){
				System.out.println("Bootstrap "+toString()+" on "+Thread.currentThread().getName()+":"+Thread.currentThread().getId()+" blocking for external component start.");
				waits++;
				if(waits>30){
					throw new RuntimeException("Bootstrap "+toString()+" on "+Thread.currentThread().getName()+":"+Thread.currentThread().getId()+" timed out waiting for external component start.");
				}
				try{
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
			}
			System.out.println("Bootstrap "+toString()+" on "+Thread.currentThread().getName()+":"+Thread.currentThread().getId()+" unblocked by successful external component start.");
			return;
		}
		
		super.start();
		
		boolean logEverything = false;
		try {
			logEverything = "CONSOLE".equalsIgnoreCase(GoGoEgo.getInitProperties().getProperty(Constants.PROPERTY_CONSOLE_LOGGING));
		} catch (NullPointerException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		if (!logEverything) {
			Logger restlet = Logger.getLogger("org.restlet");
			restlet.setLevel(Level.SEVERE);
			restlet.setUseParentHandlers(false);
		}
		
		if(started.compareAndSet(STARTING, STARTED)){
			System.out.println("Bootstrap "+toString()+" on "+Thread.currentThread().getName()+":"+Thread.currentThread().getId()+" started the component and signalled success.");
		} else {
			System.err.println("Bootstrap "+toString()+" on "+Thread.currentThread().getName()+":"+Thread.currentThread().getId()+" could not signal component start.  This is bad.");
		}
	}
	
	public IconProvider getIconProvider() {
		String iconMode = GoGoEgo.getInitProperties().getProperty("GOGOEGO_DESKTOP_ICON_MODE", "simple").toLowerCase();
		
		IconProvider provider;
		if ("basic".equals(iconMode)) {
			provider = new DesktopIntegration.BaseIconProvider(
				GoGoEgo.getInitProperties().getProperty("GOGOEGO_DESKTOP_ICON_BASIC_FOLDER", ""), 
				GoGoEgo.getInitProperties().getProperty("GOGOEGO_DESKTOP_ICON_BASIC_NAME", "appicon.png")
			);
		}
		else {
			provider = new DesktopIntegration.SimpleIconProvider(
				GoGoEgo.getInitProperties().getProperty("GOGOEGO_DESKTOP_ICON_SIMPLE_ICON", "appicon.png"));
		}
		return provider;
	}

	public void setRestarter(Restarter restarter) {
		this.restarter = restarter;
	}

	public void restart() {
		restarter.restart();
	}

}
