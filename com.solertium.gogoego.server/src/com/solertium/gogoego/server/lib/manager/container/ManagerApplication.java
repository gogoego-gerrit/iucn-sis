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
package com.solertium.gogoego.server.lib.manager.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.gogoego.api.classloader.SimpleClasspathResource;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.Guard;
import org.restlet.util.Resolver;

import com.solertium.gogoego.server.Bootstrap;
import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.manager.resources.AttachSiteRestlet;
import com.solertium.gogoego.server.lib.manager.resources.ListResource;
import com.solertium.gogoego.server.lib.manager.resources.PagingRestlet;
import com.solertium.gogoego.server.lib.manager.resources.PluginLogResource;
import com.solertium.gogoego.server.lib.manager.resources.PluginResource;
import com.solertium.gogoego.server.lib.manager.resources.RemoveSiteRestlet;
import com.solertium.gogoego.server.lib.manager.resources.RestartResource;
import com.solertium.gogoego.server.lib.manager.resources.UpdateSiteRestlet;

/**
 * ManagerApplication.java
 * 
 * Provides a means to manage your GoGoEgo sites without the need to restart the
 * entire server to perform services such as adding, updating, and removing
 * VirtualHosts.
 * 
 * The manager is not initialized by default, rather, the initialization
 * properties must be set in the Bootstrap component-config properties.
 * 
 * The manager also provides views of what sites are currently running on the
 * server as well as a means of refreshing a site. This is all based on the
 * hosts.xml file, which will be updated by this application.
 * 
 * @author carl.scott
 * 
 */
public class ManagerApplication extends Application {
	
	private static String INTERNAL_MANAGER_NAME;
	
	public static String getInternalManagerID() {
		return INTERNAL_MANAGER_NAME;
	}

	private final String siteID;
	private final String hostRoot;
	private String userName;
	private String password;
	private final ArrayList<String> sitesOnServer;
	private final Bootstrap bootstrap;
	private final boolean isInternal;
	
	public static class ManagerProperties {
		private String siteID;
		private String match;

		public ManagerProperties() {
			siteID = GoGoEgo.getInitProperties().getProperty("GOGOEGO_MANAGER_SITEID");
			match = GoGoEgo.getInitProperties().getProperty("GOGOEGO_MANAGER_MATCH");
			if (match == null)
				match = siteID + ".*";
		}

		public String getSiteID() {
			return siteID;
		}

		public String getMatch() {
			return match;
		}
	}

	public static ManagerProperties getManagerSiteID() {
		String onSwitch = GoGoEgo.getInitProperties().getProperty("GOGOEGO_MANAGER_SWITCH");
		String siteID = GoGoEgo.getInitProperties().getProperty("GOGOEGO_MANAGER_SITEID");

		return onSwitch != null && onSwitch.equalsIgnoreCase("true") && siteID != null ? new ManagerProperties() : null;
	}

	public ManagerApplication(final Context parentContext, final String siteID, final String hostRoot,
			final ArrayList<String> sitesOnServer, final Bootstrap bootstrap) {
		this(parentContext, siteID, hostRoot, sitesOnServer, bootstrap, false);
	}
	
	public ManagerApplication(final Context parentContext, final String siteID, final String hostRoot, 
			final ArrayList<String> sitesOnServer, final Bootstrap bootstrap, final boolean isInternal) {
		super(parentContext);
		
		this.siteID = siteID;
		this.hostRoot = hostRoot;
		this.userName = GoGoEgo.getInitProperties().getProperty("GOGOEGO_MANAGER_USERNAME");
		if (userName == null)
			userName = "manager";
		this.password = GoGoEgo.getInitProperties().getProperty("GOGOEGO_MANAGER_PASSWORD");
		if (password == null)
			password = "changeme";
		this.sitesOnServer = sitesOnServer;
		this.bootstrap = bootstrap;
		this.isInternal = isInternal;
		
		if (isInternal)
			INTERNAL_MANAGER_NAME = siteID;

		final String module = parentContext.getParameters().getFirstValue("module");
		if (module != null)
			getContext().getParameters().add("module", module);

		GoGoDebug.get("config").println("---- ** Building Manager ** ----");
	}

	public Restlet createRoot() {
		Guard guard = new Guard(getContext(), ChallengeScheme.HTTP_BASIC, siteID);
		guard.setSecretResolver(new Resolver<char[]>() {
			public char[] resolve(String username) {
				if (userName.equals(username))
					return password.toCharArray();
				else
					return null;
			}
		});

		Router root = new Router(getContext());
		root.attachDefault(new MagicDisablingFilter(getContext(), ClientResource.class));

		root.attach("/addSite", new AttachSiteRestlet(getContext(), hostRoot));
		root.attach("/updateSite/{siteID}", new UpdateSiteRestlet(getContext(), hostRoot));

		RemoveSiteRestlet remove = new RemoveSiteRestlet(getContext(), hostRoot);
		root.attach("/removeSite/{siteID}", remove);
		root.attach("/removeSite/{siteID}/{vKey}", remove);

		root.attach("/list", ListResource.class);
		
		/*
		 * 1.3 Logging.
		 */
		final PagingRestlet consolePaging = 
			new PagingRestlet(getContext(), getVMRoot(), ".debug");
		
		root.attach("/display/console/{siteID}/page", consolePaging);
		root.attach("/display/console/{siteID}/page/{key}/{num}", consolePaging);
		
		final PagingRestlet loggerPaging = 
			new PagingRestlet(getContext(), getVMRoot(), ".log");
		
		root.attach("/display/logger/{siteID}/page", loggerPaging);
		root.attach("/display/logger/{siteID}/page/{key}/{num}", loggerPaging);
		
		/*
		 * 1.2 Logging Display Support.  I'd rather this not make it into the 
		 * 1.3 release.
		 */
		root.attach("/console/{siteID}", new Restlet(getContext()) {
			public void handle(Request request, Response response) {
				try {
					response.setEntity(new InputRepresentation(new FileInputStream(new File(
						getVMRoot() + File.separator + request.getAttributes().get("siteID") + ".debug"
					)), MediaType.TEXT_PLAIN));
				} catch (IOException e) {
					response.setEntity(new StringRepresentation(
						"Console unavailable, please try again later.", MediaType.TEXT_PLAIN
					));
				}
				
				response.setStatus(Status.SUCCESS_OK);
			}
		});
		
		root.attach("/logger/{siteID}", new Restlet(getContext()) {
			public void handle(Request request, Response response) {
				try {
					response.setEntity(new InputRepresentation(new FileInputStream(new File(
						getVMRoot() + File.separator + request.getAttributes().get("siteID") + ".log"
					)), MediaType.TEXT_PLAIN));
				} catch (IOException e) {
					response.setEntity(new StringRepresentation(
						"Log unavailable, please try again later.", MediaType.TEXT_PLAIN
					));
				}
				
				response.setStatus(Status.SUCCESS_OK);
			}
		});
		/* end 1.2 logging display support */
		
		root.attach("/log", PluginLogResource.class);
		root.attach("/log/{plugin}", PluginLogResource.class);
		
		root.attach("/plugins/store", new Redirector(getContext(), "http://gogostore.edgy.gogoego.com/"));
		root.attach("/plugins/list", PluginResource.class);
		root.attach("/plugins/{plugin}/{version}", PluginResource.class);

		root.attach("/restart", new Restlet() {
			public void handle(Request request, Response response) {
				try {
					restartComponent();
					response.setStatus(Status.SUCCESS_OK);
					response.setEntity(new StringRepresentation(
							"<html><head><body>Restarting GoGoEgo container</body></html>", MediaType.TEXT_HTML));
				} catch (RuntimeException re) {
					re.printStackTrace();
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
					response.setEntity(new StringRepresentation(
							"<html><head><body>Could not restart due to error.</body></html>", MediaType.TEXT_HTML));
				}
			}
		});

		root.attach("/restart/{siteID}", RestartResource.class);
//		return root;
		if (System.getProperty("HOSTED_MODE", "false").equals("true"))
			return root;		
		else if (isInternal)
			return root;
		else {
			guard.setNext(root);
			return guard;
		}
	}

	public void restartComponent() {
		bootstrap.restart();
	}

	public ArrayList<String> getSitesOnServer() {
		return sitesOnServer;
	}

	public Bootstrap getBootstrap() {
		return bootstrap;
	}
	
	public String getVMRoot(){
		return hostRoot;
	}

	public static class ClientResource extends SimpleClasspathResource {
		
		public ClientResource(Context context, Request request, Response response) {
			super(context, request, response);
		}
		
		public String getBaseUri() {
			return "com/solertium/gogoego/client/manager/compiled/public/com.solertium.gogoego.client.manager.GoGoEgoManagerClient";
		}
		
		public ClassLoader getClassLoader() {
			return PluginAgent.getClassLoaderBroker().getPlugin("com.solertium.gogoego.client.manager.compiled");
		}
		
	}
}
