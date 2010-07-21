/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */

package com.solertium.gogoego.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.applications.HasPluginUI;
import org.gogoego.api.applications.HasSettingsUI;
import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.authentication.AuthenticatorFactory;
import org.gogoego.api.authentication.CookieAuthenticatorFactory;
import org.gogoego.api.classloader.SimpleClasspathResource;
import org.gogoego.api.collections.CategoryData;
import org.gogoego.api.collections.CollectionRefactoring;
import org.gogoego.api.collections.CollectionResourceBuilder;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.service.LogService;
import org.restlet.util.Template;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.applications.GoGoEgoApplicationLoader;
import com.solertium.gogoego.server.applications.GoGoEgoApplicationManager;
import com.solertium.gogoego.server.applications.ServerApplicationImpl;
import com.solertium.gogoego.server.auth.GoGoCookieAuthenticationFilter;
import com.solertium.gogoego.server.auth.GoGoGuard;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.connectors.FCKRedirectRestlet;
import com.solertium.gogoego.server.filters.CollectionCacheFilter;
import com.solertium.gogoego.server.filters.CollectionLastModifiedProtectionFilter;
import com.solertium.gogoego.server.filters.ConditionalGetFilter;
import com.solertium.gogoego.server.filters.FileWritingPluginFilter;
import com.solertium.gogoego.server.filters.PluggablePreFilter;
import com.solertium.gogoego.server.filters.TemplateRegistryUpdateFilter;
import com.solertium.gogoego.server.filters.VFSLastModifiedProtectionFilter;
import com.solertium.gogoego.server.lib.caching.CacheDumpResource;
import com.solertium.gogoego.server.lib.caching.MemoryCacheEvictionFilterFactory;
import com.solertium.gogoego.server.lib.clienttools.AdminToolResource;
import com.solertium.gogoego.server.lib.clienttools.ImageResize;
import com.solertium.gogoego.server.lib.clienttools.UploadRestlet;
import com.solertium.gogoego.server.lib.clienttools.VersionDiff;
import com.solertium.gogoego.server.lib.collections.restlets.CollectionFilter;
import com.solertium.gogoego.server.lib.collections.restlets.WritableCollectionResource;
import com.solertium.gogoego.server.lib.editing.ScriptingResource;
import com.solertium.gogoego.server.lib.resources.GoGoEgoVFSResource;
import com.solertium.gogoego.server.lib.resources.StaticPageTreeNode;
import com.solertium.gogoego.server.lib.resources.ViewRegistryResource;
import com.solertium.gogoego.server.lib.services.ApplicationEvents;
import com.solertium.gogoego.server.lib.settings.SimpleSettingsStorage;
import com.solertium.gogoego.server.lib.settings.resources.InstallationRestlet;
import com.solertium.gogoego.server.lib.settings.resources.ShortcutsSettingsResource;
import com.solertium.gogoego.server.lib.settings.resources.SimpleSettingsAuthorityResource;
import com.solertium.gogoego.server.lib.settings.resources.SimpleSettingsDataResource;
import com.solertium.gogoego.server.lib.settings.resources.SimpleSettingsInitResource;
import com.solertium.gogoego.server.lib.settings.resources.UninstallationRestlet;
import com.solertium.gogoego.server.lib.templates.TemplateRegistry;
import com.solertium.gogoego.server.lib.templates.TemplateResource;
import com.solertium.gogoego.server.security.CollectionsSecurityFilter;
import com.solertium.gogoego.server.security.GoGoSecurityFilter;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.BoundedHashMap;
import com.solertium.util.NodeCollection;
import com.solertium.util.SysDebugger;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.CookieUtility;
import com.solertium.util.restlet.FastRouter;
import com.solertium.util.restlet.HasInstanceId;
import com.solertium.util.restlet.ScratchResourceBin;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.Dav2VFSResource;
import com.solertium.vfs.restlet.FCKConnectorRestlet;
import com.solertium.vfs.restlet.SecurityFilter;
import com.solertium.vfs.restlet.VFSProvidingApplication;
import com.solertium.vfs.restlet.VFSVersionAccessResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * ServerApplication.java
 * 
 * The main application for GoGoEgo sites.  Serves public and 
 * private assets on a per-site basis and manages GoGoEgo Applications. 
 * 
 * @author rob.heittman
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ServerApplication extends Application implements VFSProvidingApplication, HasInstanceId {

	public static final int GOGOEGO_LOG_LEVEL = SysDebugger.ALL;
	public static boolean SYSDEBUGGER_IS_CONFIGURED = false;
	
	public static final String PUBLIC_FILE_LOCATION = 
		"/apps/com.solertium.gogoego.server/files";

	public final ConcurrentHashMap<String, GoGoEgoApplication> installedApplications;

	public static ServerApplication getFromContext(Context context) {
		ServerApplication sa = (ServerApplication) context.getAttributes()
				.get(VFSProvidingApplication.INITIALIZING_KEY);
		if (sa == null)
			sa = (ServerApplication) Application.getCurrent();
		return sa;
	}
	
	public static GoGoEgoApplication getFromContext(Context context, String registrationKey) {
		return getFromContext(context).getInstalledApplication(registrationKey);
	}
	
	protected final String siteID;
	protected final String httpsHost;
	protected final String username;
	protected final String password;
	protected final String repositoryURI;
	protected final String canonicalHostDomain;
	protected final ScratchResourceBin scratchResourceBin;
	protected final ApplicationEvents events;
	protected final SimpleSettingsStorage settingsStorage;
	protected final Map<String, StaticPageTreeNode> lastModifiedMap;

	protected final String applicationKey;
	protected Document clientApps;
	
	protected TemplateRegistry templateRegistry = null;

	protected final VFS vfs;
	protected final File storageRoot;
	
	protected final MyGoGoEgoApplicationLoader applicationLoader;
	
	protected final Router publicRouter, resourceRouter, privateRouter;
	protected GoGoGuard gogoGuard;
	protected GoGoCookieAuthenticationFilter cookieGuard;

	ServerApplication(final Context parentContext, final String siteID, final File storageRoot, final VFS vfs, final String httpsHost,
			final String username, final String password, final String repositoryURI, final String canonicalHostDomain) {
		super(parentContext);

		this.siteID = siteID;
		this.username = username;
		this.password = password;
		this.repositoryURI = repositoryURI;
		this.canonicalHostDomain = canonicalHostDomain;
		this.httpsHost = httpsHost;
		this.storageRoot = storageRoot;
		this.applicationKey = CookieUtility.newUniqueID();
		this.events = new ApplicationEvents(parentContext);
		this.settingsStorage = new SimpleSettingsStorage();
		this.lastModifiedMap = new BoundedHashMap<String, StaticPageTreeNode>(500);
		
		GoGoDebug.init(siteID);
		
		resourceRouter = new FastRouter(getContext());
		publicRouter = new FastRouter(getContext());
		privateRouter = new FastRouter(getContext());

		installedApplications = new ConcurrentHashMap<String, GoGoEgoApplication>();

		final String module = parentContext.getParameters().getFirstValue("module");
		if (module != null)
			getContext().getParameters().add("module", module);

		config().println("--------- Building Server Application ---------");
		config().println("Site: {0}; HTTPS: {1}", siteID, httpsHost);

		scratchResourceBin = new ScratchResourceBin();

		this.vfs = vfs;

		setStatusService(new CustomStatusService(vfs));

		prepareTemplateRegistry();

		// In response to range bugs
		getRangeService().setEnabled(false);

		applicationLoader = new MyGoGoEgoApplicationLoader(vfs);

		config().println("-------- Done Building Server Application --------");
	}
	
	private GoGoDebugger config() {
		return GoGoDebug.get("config", siteID);
	}

	@Override
	/**
	 * Currently the wiring looks kind of like this:
	 * 
	 * canonicalRedirectFilter
	 *   logFilter
	 *     gzipper
	 *       magic
	 *         rootRouter
	 *           publicRouter
	 *           privateRouter
	 *           
	 */
	public Restlet createRoot() {
		getContext().getAttributes().put(INITIALIZING_KEY, this);
		config().println("- Creating root for {0} -", siteID);
		final Filter gzipper = new GzipFilter(getContext());
		final Filter magic = new GoGoMagicFilter(getContext());

		attachPublicRouter(publicRouter);

		prepareApplications(publicRouter, privateRouter);

		prepareShortcuts();
		
		/*
		 * This will now be called by a callback from the application loader.
		 */
		// prepareScriptableObjects();
		final Router rootRouter = new FastRouter(getContext());
		rootRouter.attach(publicRouter);
		rootRouter.attach("/admin", attachPrivateRouter(privateRouter));

		gzipper.setNext(magic);
		magic.setNext(rootRouter);

		try {

			VFSPath systemPath = new VFSPath("/(SYSTEM)");
			VFSMetadata sys = vfs.getMetadata(systemPath);
			sys.setHidden(true);
			/*
			 * Add this back once we add a private means of 
			 * accessing views and template registry in the 
			 * GoGoEgo Admin Client...
			 */
			sys.addSecurityProperty(VFSMetadata.SECURE_REJECT_ALL, "true");
			vfs.setMetadata(systemPath, sys);

			VFSPath appsPath = new VFSPath("/(SYSTEM)/apps.xml");
			VFSMetadata sysApps = vfs.getMetadata(appsPath);
			sysApps.setHidden(true);
			sysApps.addSecurityProperty(VFSMetadata.SECURE_REJECT_ALL, "true");
			vfs.setMetadata(appsPath, sysApps);

			VFSMetadata templates = vfs.getMetadata(TemplateRegistry.REGISTRY_PATH);
			templates.setVersioned(false);
			vfs.setMetadata(TemplateRegistry.REGISTRY_PATH, templates);

		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		getContext().getAttributes().remove(INITIALIZING_KEY);
		config().println("- Done creating root for {0} -", siteID);
		
		final Logger logger = Logger.getLogger("com.solertium.gogoego.server.ServerApplication."+siteID);
		logger.addHandler((Handler)GoGoDebug.get("log", siteID));
		boolean logEverything = false;
		try {
			logEverything = "CONSOLE".equalsIgnoreCase(GoGoEgo.getInitProperties().getProperty(Constants.PROPERTY_CONSOLE_LOGGING));
		} catch (NullPointerException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		logger.setUseParentHandlers(logEverything);
		
		
		final LogService logService = new LogService(true);
		logService.setLoggerName("com.solertium.gogoego.server.ServerApplication." + siteID);
		
		final Filter logFilter = logService.createInboundFilter(getContext());
		logFilter.setNext(new ConditionalGetFilter(getContext(), gzipper));
		
		if (canonicalHostDomain != null && !canonicalHostDomain.equals("")) {
			final CanonicalRedirectFilter filter = 
				new CanonicalRedirectFilter(getContext(), canonicalHostDomain);
			filter.setNext(logFilter);
			
			return filter;
		}
		else
			return logFilter;
	}

	/**
	 * Private facilities are exposed here. Access to these facilities is
	 * protected by a guard which passes internal (RIAP) calls or properly
	 * authenticated HTTP calls.
	 * 
	 * cookieGuard
	 *   gogoGuard
	 *     privateRouter
	 */
	protected Restlet attachPrivateRouter(Router privateRouter) {
		gogoGuard = new GoGoGuard(getContext(), ChallengeScheme.HTTP_BASIC, siteID);
		gogoGuard.setNext(privateRouter);
		
		cookieGuard = new GoGoCookieAuthenticationFilter(getContext(), siteID);
		cookieGuard.setNext(gogoGuard);

		privateRouter.attach("/apps",
				new MagicDisablingFilter(getContext(), new ApplicationRegistryFetcher(getContext())));

		privateRouter.attach("/apps/install", new MagicDisablingFilter(getContext(),
				new InstallationRestlet(getContext())));
		privateRouter.attach("/apps/uninstall", new MagicDisablingFilter(getContext(), new UninstallationRestlet(
				getContext())));

		privateRouter.attachDefault(new MagicDisablingFilter(getContext(), ClientResource.class));
		
		privateRouter.attach("/cache", CacheDumpResource.class);
		
		// Writing to collections
		final CollectionLastModifiedProtectionFilter coll_lmFilter = new CollectionLastModifiedProtectionFilter(
				getContext(), "timestamp", false);
		coll_lmFilter.setNext(new Finder(getContext(), WritableCollectionResource.class));

		privateRouter.attach("/collections", MemoryCacheEvictionFilterFactory.newInstance(getContext(), coll_lmFilter));

		privateRouter.attach("/management/refactorcollections/{viewID}", new Restlet(getContext()) {
			public void handle(Request request, Response response) {
				// This could take a while...
				new Thread(new CollectionRefactoring((String) request.getAttributes().get("viewID"), getVFS())).start();
				response.setStatus(Status.SUCCESS_NO_CONTENT);
			}
		});
		privateRouter.attach("/manager", new MagicDisablingFilter(getContext(), AdminToolResource.class));

		// DAV
		/*
		 * This has grown to look like the following:
		 * 
		 * TemplateRegistryUpdateFilter
		 *   MemoryCacheEvictionFilter
		 *     Security Filter
		 *       File Writing Plugin Filter
		 *         Last Modified Protection Filter (TODO: integrate with the above)
		 *           Magic Disabling Filter
		 *             DAV2VFSREsource
		 */
		final VFSLastModifiedProtectionFilter lmFilter = new VFSLastModifiedProtectionFilter(getContext(), "timestamp",
				false);
		lmFilter.setNext(new MagicDisablingFilter(getContext(), new Finder(getContext(), Dav2VFSResource.class)));

		final GoGoSecurityFilter filter = new GoGoSecurityFilter(getContext(), applicationKey);
		filter.setNext(new FileWritingPluginFilter(getContext(), lmFilter));

		final TemplateRegistryUpdateFilter update = new TemplateRegistryUpdateFilter(getContext());
		update.setNext(MemoryCacheEvictionFilterFactory.newInstance(getContext(), filter));
		
		privateRouter.attach("/files", update);

		// Edit Area
		privateRouter.attach("/js/editarea", new MagicDisablingFilter(getContext(), EditAreaResource.class));

		// FCK Editor Integration
		privateRouter.attach("/js/forkeditor/editor/filemanager/connectors/restlet/connector.restlet",
				new MagicDisablingFilter(getContext(), new FCKConnectorRestlet(getContext())));

		FCKRedirectRestlet redirectRestlet = new FCKRedirectRestlet(getContext());
		redirectRestlet.setNext(FCKClientResource.class);
		redirectRestlet.setBaseUrl("/admin/js/forkeditor");
		redirectRestlet.addSupportedPath("/fckstyles.xml");
		redirectRestlet.addSupportedPath("/fckconfig.js");
		privateRouter.attach("/js/forkeditor", new MagicDisablingFilter(getContext(), redirectRestlet));

		// Surfing and Editing
		
		privateRouter.attach("/js/edit.js", new MagicDisablingFilter(getContext(), ScriptingResource.class));
		privateRouter.attach("/js/surf.js", new MagicDisablingFilter(getContext(), ScriptingResource.class));
		
		privateRouter.attach("/registry/templates", new MagicDisablingFilter(getContext(), new TemplateRegistryFetcher(getContext())));
		privateRouter.attach("/registry/views", new MagicDisablingFilter(getContext(), ViewRegistryResource.class));
		
		privateRouter.attach("/revisions", new MagicDisablingFilter(getContext(), new Finder(getContext(),
				VFSVersionAccessAndRevertResource.class)));

		// Settings
		privateRouter.attach("/settings/shortcuts", ShortcutsSettingsResource.class);
		privateRouter.attach("/settings/{worker}", SimpleSettingsInitResource.class);
		privateRouter.attach("/settings/{worker}/authority/{remaining}", SimpleSettingsAuthorityResource.class);
		privateRouter.attach("/settings/{worker}/data/{remaining}", SimpleSettingsDataResource.class);

		// Tools for the admin section
		privateRouter.attach("/tools/diff", new MagicDisablingFilter(getContext(), new VersionDiff(
				getContext())));
		privateRouter.attach("/tools/imgresize", ImageResize.class);
		privateRouter.attach("/tools/templateRegistry", TemplateResource.class);

		// Upload from admin section
		privateRouter.attach("/upload", new UploadRestlet(getContext()));

		return cookieGuard;
	}

	/**
	 * The public view of the resource space is exposed here.
	 */
	protected void attachPublicRouter(Router publicRouter) {
		//final Router resourceRouter = new FastRouter(getContext());
		/*
		 * First, the plain VFS resource...
		 */
		final SecurityFilter fileFilter = new GoGoSecurityFilter(getContext(), applicationKey);
		fileFilter.setNext(GoGoEgoVFSResource.class);

		resourceRouter.attachDefault(fileFilter);
		
		/*
		 * Because of changes to FastRouter and shortcuts, we must 
		 * now also mount VFS shortcuts at an external router.
		 */
		resourceRouter.attach(PUBLIC_FILE_LOCATION, fileFilter);

		/*
		 * Now, for collections...
		 */
		final SecurityFilter collectionFilter = new CollectionsSecurityFilter(getContext(), applicationKey);
		collectionFilter.setNext(new CollectionCacheFilter(getContext(), new CollectionFilter(getContext(), applicationKey)));

		resourceRouter.attach("/collections", collectionFilter);

		/*
		 * Finally, attach the view filter atop everything...
		 */

		final ViewFilter viewFilter = new ViewFilter(getContext());
		viewFilter.setNext(resourceRouter);

		publicRouter.attachDefault(new PluggablePreFilter(getContext(), viewFilter));
		//publicRouter.attach(TemplateRegistry.REGISTRY_PATH.toString(), new TemplateRegistryFetcher(getContext()));

		// FIXME: new way of doing this
		// vfs.addListener(new VFSPath("/templates"), templateRegistryListener);
	}

	private Map<String, String> shortcuts = new HashMap<String, String>();
	private Collection<Restlet> restletReferences = new ArrayList<Restlet>();

	/**
	 * Find a hard-linked shortcut
	 * @param match the possible shortcut
	 * @return the link if found, null otherwise
	 */
	public String getShortcut(String match) {
		return (shortcuts.get(match.toLowerCase()));
	}

	/**
	 * Configure shortcuts.
	 */
	public void prepareShortcuts() {
		config().println("Preparing shortcuts.");
		
		final Collection<Restlet> iRedirectors = new ArrayList<Restlet>();
		
		Document document;
		try {
			document = vfs.getDocument(new VFSPath("/(SYSTEM)/shortcuts.xml"));
		} catch (IOException e) {
			document = null;
		}

		if (document == null)
			return;
		
		for (Restlet r : restletReferences)
			resourceRouter.detach(r);

		NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
		for (Node node : nodes) {
			if (node.getNodeType() == Node.TEXT_NODE)
				continue;
			final String shortcut = DocumentUtils.impl.getAttribute(node, "shortcut");
			
			final String resource = DocumentUtils.impl.getAttribute(node, "resource");
			final String arbitrary = DocumentUtils.impl.getAttribute(node, "arbitrary");
			final String redirect = DocumentUtils.impl.getAttribute(node, "redirect");
			final String root = DocumentUtils.impl.getAttribute(node, "root");
			final String collection = DocumentUtils.impl.getAttribute(node, "collection");			
			
			if (shortcut.equals(""))
				continue;

			if (!resource.equals("")) {
				final VFSPath path;
				try {
					path = VFSUtils.parseVFSPath(resource);
				} catch (VFSUtils.VFSPathParseException e) {
					continue;
				}

				/*
				 * This must use the public file location in order to ensure 
				 * that there are no clashes with shortcuts. Potential clashes, 
				 * for exmaple, are when a shortcut to a page at "/myhtmlpage.html" 
				 * is shortcutted to "/myhtmlpage".  This will cause an infinite 
				 * loop as the shortcut will eat it.  Now, it is redirected to 
				 * 
				 * ${PUBLIC_FILE_LOCATION}/myhtmlpage.html
				 * 
				 * 2009-10-20 CS
				 * 
				 */
				final Restlet redirector = 
					new PersistentRedirector(getContext(), "riap://host" + PUBLIC_FILE_LOCATION + path.toString(), Redirector.MODE_DISPATCHER);
				iRedirectors.add(redirector);
				resourceRouter.attach(shortcut, redirector);
	
				//iSoftRedirects.put(shortcut.toLowerCase(), "riap://host" + path.toString());
			
				config().println("Added resource shortcut {0} -> {1}", shortcut, resource);
			}
			else if (!arbitrary.equals("")) {
				final VFSPath path;
				try {
					path = VFSUtils.parseVFSPath(arbitrary);
				} catch (VFSUtils.VFSPathParseException e) {
					continue;
				}
				
				final Restlet redirector = 
					new PersistentRedirector(getContext(), "riap://host" + path.toString(), Redirector.MODE_DISPATCHER);
				iRedirectors.add(redirector);
				resourceRouter.attach(shortcut, redirector);
				
				//iSoftRedirects.put(shortcut.toLowerCase(), "riap://host" + path.toString());
			
				config().println("Added arbitrary shortcut {0} -> {1}", shortcut, arbitrary);
			}
			else if (!redirect.equals("")) {
				final Restlet redirector = new PersistentRedirector(getContext(), 
					"riap://host" + redirect, Redirector.MODE_DISPATCHER);
				iRedirectors.add(redirector);
				resourceRouter.attach(shortcut, redirector);
				config().println("Added redirect shortcut {0} -> {1}", shortcut, redirect);
			}
			else if (!collection.equals("")) {
				String itemTemplate = DocumentUtils.impl.getAttribute(node, "itemTemplate");
				if ("".equals(itemTemplate))
					itemTemplate = null;
				String categoryTemplate = DocumentUtils.impl.getAttribute(node, "categoryTemplate");
				if ("".equals(categoryTemplate))
					categoryTemplate = null;
				final Restlet redirector = new CollectionShortcutPersistentRedirector(getContext(), 
					"riap://host" + collection + "{rr}", itemTemplate, categoryTemplate, Redirector.MODE_DISPATCHER);
				iRedirectors.add(redirector);
				resourceRouter.attach(shortcut, redirector);
				config().println("Added collection shortcut {0} -> {1}", shortcut, collection);
			}
			else if (!root.equals("")) {
				//TODO: get this from settings
				final Restlet redirector = 
					new PersistentRedirector(getContext(), "riap://host" + root, Redirector.MODE_DISPATCHER);
				iRedirectors.add(redirector);
				resourceRouter.attach("/index.html", redirector);
				
				//iSoftRedirects.put("/index.html", "riap://host" + root);
				
				config().println("Added root shortcut {0} -> {1}", shortcut, root);
			}
			else
				config().println("Found no mapping for shortcut {0}", shortcut);
		}
		//shortcuts = iSoftRedirects;
		restletReferences = iRedirectors;
	}

	public String getHttpsHost() {
		return httpsHost;
	}
	
	/**
	 * This mechanism acquires storage appropriate to the site servicing the
	 * current thread.  This is useful for plugins that need persistent disk
	 * storage to support libraries that produce indices, databases, etc.
	 * 
	 * By default, this space is allocated adjacent to the vfs root for the site,
	 * in "storage/[key]" where the key should be specific to the requesting
	 * bundle to minimize collisions.
	 * 
	 * However, this layout is not guaranteed; a more complex installation may
	 * provide physically separate storage from the VFS.
	 * 
	 * @deprecated Ideally, you don't want to use filesystem access, but File
	 *   centric existing libraries may force it upon you.
	 * @param key A bundle specific key, e.g. "org.apache.lucene.index"
	 * @return A guaranteed site-specific Directory for arbitrary storage.
	 */
	public static File getCurrentFileStorage(String key) {
		return ((ServerApplication) Application.getCurrent()).getFileStorage(key);
	}
	
	/**
	 * This mechanism acquires arbitrary storage appropriate to this application.
	 * 
	 * @see #getCurrentFileStorage(String)
	 * 
	 * @deprecated Ideally, you don't want to use filesystem access, but File
	 *   centric existing libraries may force it upon you.
	 * @param key A bundle specific key, e.g. "org.apache.lucene.index"
	 * @return A guaranteed site-specific Directory for arbitrary storage.
	 */
	public File getFileStorage(String key) {
		File ps = new File(storageRoot,key);
		if(!ps.exists()) ps.mkdirs();
		return ps;
	}

	public ScratchResourceBin getScratchResourceBin() {
		return scratchResourceBin;
	}

	public TemplateRegistry getTemplateRegistry() {
		return templateRegistry;
	}

	public ApplicationEvents getApplicationEvents() {
		return events;
	}
	
	public SimpleSettingsStorage getSettingsStorage() {
		return settingsStorage;
	}

	public VFS getVFS() {
		return vfs;
	}
	
	public String getInstanceId() {
		return siteID;
	}
	
	public String getSiteID() {
		return getInstanceId();
	}
	
	public Map<String, StaticPageTreeNode> getLastModifiedMap() {
		return lastModifiedMap;
	}

	private Boolean isHostedMode = null;

	public boolean isHostedMode() {
		if (isHostedMode == null)
			isHostedMode = "true".equalsIgnoreCase(System.getProperty("HOSTED_MODE"));
		return isHostedMode;
	}

	/**
	 * Configure GoGoEgo Applications
	 * @param publicRouter
	 * @param privateRouter
	 */
	public void prepareApplications(Router publicRouter, Router privateRouter) {
		clientApps = DocumentUtils.impl.newDocument();

		final Element clientAppDocRoot = clientApps.createElement("root");
		clientAppDocRoot.appendChild(DocumentUtils.impl.createElementWithText(clientApps, "applicationKey",
				applicationKey));

		clientApps.appendChild(clientAppDocRoot);

		try {
			applicationLoader.load();
		} catch (NotFoundException e) {
			TrivialExceptionHandler.ignore(this, e);
			applicationLoader.onLoadComplete();
		}

		clientApps = applicationLoader.getDocument();
	}

	/**
	 * Attempting to use this function to fetch an uninstalled application will
	 * cause a RuntimeException. If you want to check to see if an application
	 * is installed or not, use isApplicationInstalled()
	 * 
	 * @see isApplicationInstalled()
	 * @param appName
	 * @return
	 */
	public GoGoEgoApplication getInstalledApplication(final String registrationKey) {
		GoGoEgoApplication app = installedApplications.get(registrationKey);
		if (app != null)
			return app;
		else
			throw new RuntimeException(registrationKey + " is not installed!");
	}

	/**
	 * Determine if a GoGoEgo application is installed
	 * @param registrationKey the key
	 * @return true if so, false otherwise
	 */
	public boolean isApplicationInstalled(final String registrationKey) {
		if (registrationKey == null)
			return false;
		return installedApplications.containsKey(registrationKey);
	}
	
	/**
	 * Load a new instance of an authenticator into the guard. 
	 * @param registrationKey 
	 * @param factory
	 */
	public void reloadAuthenticator(String registrationKey, AuthenticatorFactory factory) {
		gogoGuard.updateAuthenticator(registrationKey, factory);
	}
	
	/**
	 * Remove an existing authenticator from the guard.
	 * @param registrationKey
	 */
	public void removeAuthenticator(String registrationKey) {
		gogoGuard.removeAuthenticator(registrationKey);
	}
	
	/**
	 * Load a new instance of an authenticator into the guard. 
	 * @param registrationKey 
	 * @param factory
	 */
	public void reloadCookieAuthenticator(String registrationKey, CookieAuthenticatorFactory factory) {
		cookieGuard.updateAuthenticator(registrationKey, factory);
	}
	
	/**
	 * Remove an existing authenticator from the guard.
	 * @param registrationKey
	 */
	public void removeCookieAuthenticator(String registrationKey) {
		cookieGuard.removeAuthenticator(registrationKey);
	}

	/**
	 * Creates a new instance of the application, which should 
	 * bodily replace any existing instance.
	 * @param registrationKey
	 */
	public void reloadApplication(String registrationKey) {
		try {
			applicationLoader.reload(registrationKey);
		} catch (NotFoundException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	/**
	 * Remove an GoGoEgo Application and it's restlets
	 * @param registrationKey
	 */
	public void removeApplication(String registrationKey) {
		if (isApplicationInstalled(registrationKey)) {
			GoGoEgoApplication app = getInstalledApplication(registrationKey);
			applicationLoader.stopApplication(app.getPath());
			
			installedApplications.remove(registrationKey);
			
			/*
			 * Now try to uninstall it ... if this fails, meh, as far 
			 * as we're concerned, this application does not exist.
			 */
			try {
				GoGoEgoApplicationManager manager = new GoGoEgoApplicationManager(getVFS());
				manager.uninstall(registrationKey);
			} catch (GoGoEgoApplicationException e) {
				TrivialExceptionHandler.ignore(this, e);
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
			
			config().println("{0} successfully uninstalled", registrationKey);
		}
	}

	/**
	 * Configure the template registry.
	 */
	public void prepareTemplateRegistry() {
		final ArrayList<String> templateRegistryPaths = new ArrayList<String>();
		templateRegistryPaths.add("/templates");
		templateRegistryPaths.add("/(SYSTEM)/templates");
		templateRegistry = new TemplateRegistry(vfs, templateRegistryPaths) {
			protected GoGoDebugger log() {
				return config();
			}
		};
		/* templateRegistryListener = new VFSListener() {
			public void notifyEvent(VFSEvent vfsEvent) {
				config().println("The following templates were updated:");
				if (vfsEvent.getURIs() != null)
					for (VFSPath path : vfsEvent.getURIs())
						config().println("{0}", path);
				config().println("Refreshing registry...");
				templateRegistry.refresh();
			}
		}; */
	}
	
	/**
	 * ClientResource
	 * 
	 * Serves the GoGoEgo Client.
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	public static class ClientResource extends SimpleClasspathResource {
		
		private final String baseUri;
		private final String plugin;
		private final boolean isOverriden;
		
		public ClientResource(Context context, Request request, Response response) {
			super(context, request, response);
			final String siteID = GoGoEgo.get().getFromContext(context).getSiteID();
			final String baseProperty = "gogoego.config." + siteID + ".admin.client";
			
			String plugin = GoGoEgo.getInitProperties().getProperty(baseProperty + ".plugin");
			if (isOverriden = plugin != null) {
				this.baseUri = GoGoEgo.getInitProperties().getProperty(baseProperty + ".base_uri", 
					"com/solertium/gogoego/client/compiled/public/com.solertium.gogoego.client.GoGoEgoClient");
				this.plugin = plugin;
			}
			else {
				this.baseUri = "com/solertium/gogoego/client/compiled/public/com.solertium.gogoego.client.GoGoEgoClient";
				this.plugin = "com.solertium.gogoego.client.compiled";
			}
		}
		
		public Representation represent(Variant variant) throws ResourceException {
			if (isOverriden)
				return super.represent(variant);
			
			final GoGoEgoBaseRepresentation representation = (GoGoEgoBaseRepresentation)super.represent(variant);
			
			String encodedUri = getRequest().getResourceRef().getRemainingPart();
			if ("".equals(encodedUri) || "/".equals(encodedUri))
				encodedUri = "/index.html";
			
			int qindex = encodedUri.indexOf("?");
			if (qindex != -1)
				encodedUri = encodedUri.substring(0, qindex);
			try {
				encodedUri = URLDecoder.decode(encodedUri, "UTF-8");
			} catch (UnsupportedEncodingException ux) {
				throw new RuntimeException("Expected UTF-8 encoding not found in Java runtime");
			}
			
			if (!"/index.html".equals(encodedUri))
				return representation;
			
			representation.setModificationDate(Calendar.getInstance().getTime());
			
			return GoGoEgo.get().applyTemplating(
				representation, 
				new ClientTemplateRepresentation(GoGoEgo.get().getFromContext(getContext()))
			);
		}
		
		public String getBaseUri() {
			return baseUri;
		}
		
		public ClassLoader getClassLoader() {
			return PluginAgent.getClassLoaderBroker().getPlugin(plugin);
		}
		
	}
	
	public static class ClientTemplateRepresentation extends GoGoEgoStringRepresentation {
		
		private final ServerApplicationAPI api;
	
		public ClientTemplateRepresentation(ServerApplicationAPI api) {
			super("template");
			this.api = api;
			setModificationDate(Calendar.getInstance().getTime());
		}
		
		public String resolveEL(String key) {
			if ("siteID".equalsIgnoreCase(key))
				return api.getSiteID();
			else if ("version".equalsIgnoreCase(key))
				return PluginAgent.getClassLoaderBroker().getMetadata().get("com.solertium.gogoego.client.compiled").get("Bundle-Version");
			else
				return "";
		}
		
	}
	
	/**
	 * FCKClientResource
	 * 
	 * Serves the FCKEditor resources.
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	public static class FCKClientResource extends SimpleClasspathResource {
		
		public FCKClientResource(Context context, Request request, Response response) {
			super(context, request, response);
		}
		
		public String getBaseUri() {
			return "com/solertium/forkeditor";
		}
		
		public ClassLoader getClassLoader() {
			return PluginAgent.getClassLoaderBroker().getPlugin("com.solertium.gogoego.client.compiled");
		}
		
	}
	
	/**
	 * EditAreaResource
	 * 
	 * Serves the Edit Area resources
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	public static class EditAreaResource extends SimpleClasspathResource {
		
		public EditAreaResource(Context context, Request request, Response response) {
			super(context, request, response);
		}
		
		public String getBaseUri() {
			return "com/solertium/editarea";
		}
		
		public ClassLoader getClassLoader() {
			return PluginAgent.getClassLoaderBroker().getPlugin("com.solertium.gogoego.client.compiled");
		}
		
	}

	/**
	 * VFSVersionAccessAndRevertResource
	 * 
	 * Allow for revert to older versions.
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	public static class VFSVersionAccessAndRevertResource extends VFSVersionAccessResource {

		public VFSVersionAccessAndRevertResource(Context context, Request request, Response response) {
			super(context, request, response);
		}

		public boolean allowRevert() {
			return true;
		}

		public void handleRevert() {
			String version = getHeader("Version");
			if (version == null) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return;
			}

			if (!vfs.exists(uri, version)) {
				getResponse().setStatus(Status.CLIENT_ERROR_GONE);
				return;
			}

			try {
				copyStream(vfs.getInputStream(uri, version), vfs.getOutputStream(uri));
				getResponse().setStatus(Status.SUCCESS_OK);
			} catch (IOException e) {
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}

		private void copyStream(final InputStream is, final OutputStream os) throws IOException {
			final byte[] buf = new byte[65536];
			int i = 0;
			while ((i = is.read(buf)) != -1)
				os.write(buf, 0, i);
			is.close();
			os.close();
		}

		private String getHeader(final String header) {
			String ret = null;
			try {
				final org.restlet.data.Form headers = (org.restlet.data.Form) getRequest().getAttributes().get(
						"org.restlet.http.headers");
				ret = headers.getFirstValue(header);
				if (ret == null)
					ret = headers.getFirstValue(header.toLowerCase());
			} catch (final Exception poorly_handled) {
				poorly_handled.printStackTrace();
			}
			return ret;
		}
	}

	/**
	 * TemplateRegistryFetcher
	 * 
	 * Fetch a current copy of the template registry from memory. 
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	class TemplateRegistryFetcher extends Restlet {

		public TemplateRegistryFetcher(Context context) {
			super(context);
		}

		public void handle(Request request, Response response) {
			if (request.getMethod().equals(Method.GET)) {
				// Ensure a fresh copy
				templateRegistry.refresh();
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, templateRegistry.getDocument()));
			} else {
				// Template Registry can't be moved, copied, deleted, or
				// changed.
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		}
	}

	/**
	 * ApplicationRegistryFetcher
	 * 
	 * Fetch the application registry from memory
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	class ApplicationRegistryFetcher extends Restlet {

		public ApplicationRegistryFetcher(Context context) {
			super(context);
		}

		public void handle(Request request, Response response) {
			if (!request.getResourceRef().getPath().equals("/admin/apps"))
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			else if (request.getMethod().equals(Method.GET)) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, clientApps));
			} else {
				// Application Registry can't be moved, copied, deleted, or
				// changed.
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		}
	}

	/**
	 * MyGoGoEgoApplicationLoader
	 * 
	 * Implementation of the GoGoEgo Application Loader
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	class MyGoGoEgoApplicationLoader extends GoGoEgoApplicationLoader {

		private final Document document;

		public MyGoGoEgoApplicationLoader(VFS vfs) {
			super(new ServerApplicationImpl(ServerApplication.this), vfs);

			document = DocumentUtils.impl.newDocument();

			final Element clientAppDocRoot = document.createElement("root");
			clientAppDocRoot.appendChild(DocumentUtils.impl.createElementWithText(document, "applicationKey",
					applicationKey));

			document.appendChild(clientAppDocRoot);
		}

		public void onLoadComplete() {
			//
		}

		public void onFailure(String className) {
			config().println("Application {0} not added", className);
		}

		public void onSuccess(GoGoEgoApplication application, final String friendlyName, final Node data) {
			installedApplications.put(application.getRegistrationKey(), application);

			final Restlet publicRestlet = application.getPublicRouter();
			if (publicRestlet != null) {
				final Filter filter = MemoryCacheEvictionFilterFactory.newInstance(getContext());
				filter.setNext(publicRestlet);
				publicRouter.attach(application.getPath(), filter);
			}

			final Restlet privateRestlet = application.getPrivateRouter();
			if (privateRestlet != null) {
				final Filter filter = MemoryCacheEvictionFilterFactory.newInstance(getContext());
				filter.setNext(new MagicDisablingFilter(getContext(), privateRestlet));
				privateRouter.attach(application.getPath(), filter);
			}

			config().println("Successfully Added Application {0} ({1})", application.getRegistrationKey(), friendlyName);

			final Element clientNode = document.createElement("application");
			clientNode.setAttribute("bundle", application.getRegistrationKey());
			clientNode.setAttribute("name", friendlyName);

			if (application instanceof HasSettingsUI)
				clientNode.appendChild(BaseDocumentUtils.impl.createElementWithText(document, "settings",
						((HasSettingsUI) application).getSettingsURL()));
			if (application instanceof HasPluginUI)
				clientNode.appendChild(BaseDocumentUtils.impl.createElementWithText(document, "tab",
						((HasPluginUI) application).getTabURL()));
			for (Node current : new NodeCollection(data.getChildNodes()))
				clientNode.appendChild(document.importNode(current, true));

			document.getDocumentElement().appendChild(clientNode);
		}

		public void stopApplication(String path) {
			stopRestletOnRouter(publicRouter, path);
			stopRestletOnRouter(privateRouter, path);
		}

		private void stopRestletOnRouter(final Router router, final String path) {
			Restlet restlet = null;
			for (Route r : router.getRoutes()) {
				if (r.getTemplate().getPattern().startsWith(path)) {
					restlet = r.getNext();
					break;
				}
			}
			if (restlet == null)
				GoGoDebug.get("debug").println("No restlet to stop for {0}: {1}", path, restlet);
			else {
				try {
					router.detach(restlet);
					restlet.stop();
				} catch (Exception e) {
					TrivialExceptionHandler.ignore(this, e);
				}
			}
		}

		public Document getDocument() {
			return document;
		}

	}
	
	/**
	 * CanonicalRedirectFilter
	 * 
	 * For sites that should only have one canonical url-space, force a 
	 * redirect when the access the site from another url.
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	static class CanonicalRedirectFilter extends Filter {
		
		private final String canonicalDomain;
		
		public CanonicalRedirectFilter(Context context, String canonicalDomain) {
			super(context);
			this.canonicalDomain = canonicalDomain;
		}
		
		protected int beforeHandle(Request request, Response response) {
			if ((Protocol.HTTP.equals(request.getProtocol()) || Protocol.HTTPS.equals(request.getProtocol()))
					&& (!canonicalDomain.equals(request.getResourceRef().getHostDomain()))) {
				String hostPort = "";
				int port = request.getResourceRef().getHostPort();
				if (port > -1)
					hostPort = ":" + Integer.toString(port);
				
				response.redirectPermanent(
					request.getProtocol() + "://" + canonicalDomain + hostPort + 
					request.getResourceRef().getPath()
				);
				response.setEntity(new GoGoEgoStringRepresentation(
					"<p>If you are not redirected, please follow the link below:</p>" + "<a href=\""
							+ response.getLocationRef() + "\">" + response.getLocationRef() + "</a>",
					MediaType.TEXT_HTML
				));
				response.getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
				
				return Filter.STOP;
			}
			else
				return Filter.CONTINUE;
		}
		
	}
	
	/**
	 * PersistentRedirector
	 * 
	 * Keep the request headers, redirect as normal
	 * 
	 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
	 *
	 */
	static class PersistentRedirector extends Redirector {
		
		public PersistentRedirector(Context context, String targetPattern, int mode) {
			super(context, targetPattern, mode);
		}

		protected void redirectDispatcher(Reference targetRef, Request request,
	            Response response) {
	        // Save the base URI if it exists as we might need it for redirections
	        final Reference baseRef = request.getResourceRef().getBaseRef();
	        
	        // Update the request to cleanly go to the target URI
	        request.setReferrerRef(request.getResourceRef());
	        
	        // FIXME: This is a hack to accurately enable a 301 redirect down the line.
	        // There has got to be a better solution to this ...
	        request.getAttributes().put("org.gogoego.redirector.reference", request.getResourceRef());
	        
	        request.setResourceRef(targetRef);
	        
	        // PersistantRedirector will keep request headers!
	        // request.getAttributes().remove("org.restlet.http.headers");
	        getContext().getClientDispatcher().handle(request, response);

	        // Allow for response rewriting and clean the headers
	        response.setEntity(rewrite(response.getEntity()));
	        response.getAttributes().remove("org.restlet.http.headers");

	        // In case of redirection, we may have to rewrite the redirect URI
	        if (response.getLocationRef() != null) {
	        	final Template rt = new Template(this.targetTemplate);
	            rt.setLogger(getLogger());
	            final int matched = rt.parse(response.getLocationRef().toString(),
	                    request);

	            if (matched > 0) {
	                final String remainingPart = (String) request.getAttributes()
	                        .get("rr");
	                if (remainingPart != null) {
	                    response.setLocationRef(baseRef.toString() + remainingPart);
	                }
	            }
	        }
	    }
	}
	
	/*static class SoftShortcutRedirector extends PersistentRedirector {
		
		public SoftShortcutRedirector(Context context, String targetPattern, int mode) {
			super(context, targetPattern, mode);
		}

		protected void redirectDispatcher(Reference targetRef, Request request, Response response) {
			// Save the base URI if it exists as we might need it for redirections
	        final Reference baseRef = request.getResourceRef().getBaseRef();
	        
	        // Update the request to cleanly go to the target URI
	        request.setReferrerRef(request.getResourceRef());
	        request.setResourceRef(targetRef);
	        
	        // PersistantRedirector will keep request headers!
	        // request.getAttributes().remove("org.restlet.http.headers");
	        
	        getContext().getClientDispatcher().handle(request, response);

	        // Allow for response rewriting and clean the headers
	        response.setEntity(rewrite(response.getEntity()));
	        response.getAttributes().remove("org.restlet.http.headers");

	        // In case of redirection, we may have to rewrite the redirect URI
	        if (response.getLocationRef() != null) {
	        	final Template rt = new Template(this.targetTemplate);
	            rt.setLogger(getLogger());
	            final int matched = rt.parse(response.getLocationRef().toString(),
	                    request);

	            if (matched > 0) {
	                final String remainingPart = (String) request.getAttributes()
	                        .get("rr");
	                if (remainingPart != null) {
	                    response.setLocationRef(baseRef.toString() + remainingPart);
	                }
	            }
	        }
	        
	        SysDebugger.out.println("------- All done in soft shortcut! ---------");
		}
		
	}*/
	
	static class CollectionShortcutPersistentRedirector extends PersistentRedirector {
		
		private final String itemTemplate, categoryTemplate;
		private final CollectionResourceBuilder builder;
		
		public CollectionShortcutPersistentRedirector(Context context, String targetPattern, String itemTemplate, String categoryTemplate, int mode) {
			super(context, targetPattern, mode);
			this.itemTemplate = itemTemplate;
			this.categoryTemplate = categoryTemplate;
			this.builder = new CollectionResourceBuilder(context, true);
		}
		
		protected void redirectDispatcher(Reference targetRef, Request request, Response response) {
			String path = targetRef.getPath();
			if (path.endsWith("/"))
				path = path.substring(0, path.length()-2);
			final CategoryData categoryData = builder.getCurrentCategory(new VFSPath(path));
			if (categoryData != null) {
				if (itemTemplate != null && categoryData.getItemID() != null)
					path = path + "/" + itemTemplate;
				else if (categoryTemplate != null && categoryData.getItemID() == null)
					path = path + "/" + categoryTemplate;
			}
			targetRef.setPath(path);
			
	        // Save the base URI if it exists as we might need it for redirections
	        final Reference baseRef = request.getResourceRef().getBaseRef();
	        
	        // Update the request to cleanly go to the target URI
	        request.setReferrerRef(request.getResourceRef());
	        request.setResourceRef(targetRef);
	        request.getAttributes().put(Constants.SHORTCUT, Boolean.TRUE);
	        
	        
	        // PersistantRedirector will keep request headers!
	        // request.getAttributes().remove("org.restlet.http.headers");
	        getContext().getClientDispatcher().handle(request, response);
	        
	        // Allow for response rewriting and clean the headers
	        response.setEntity(rewrite(response.getEntity()));
	        response.getAttributes().remove("org.restlet.http.headers");

	        // In case of redirection, we may have to rewrite the redirect URI
	        if (response.getLocationRef() != null) {
	        	final Template rt = new Template(this.targetTemplate);
	            rt.setLogger(getLogger());
	            final int matched = rt.parse(response.getLocationRef().toString(),
	                    request);

	            if (matched > 0) {
	                final String remainingPart = (String) request.getAttributes()
	                        .get("rr");
	                if (remainingPart != null) {
	                    response.setLocationRef(baseRef.toString() + remainingPart);
	                }
	            }
	        }
	        
	        //request.getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
	    }
	}

}