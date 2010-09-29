package org.iucn.sis.server.api.application;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.resource.Resource;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

import com.solertium.util.restlet.RestletUtils;
import com.solertium.util.restlet.authentication.AuthnGuard;
import com.solertium.vfs.VFS;

public abstract class SISApplication extends GoGoEgoApplication {

	public static final String NO_TRANSACTION_HANDLE = "NO_TRAN";
	
	protected Map<Object, List<String>> pathsToResources;
	protected HashSet<String> pathsExcludedFromAuthenticator;
	protected Map<Object, List<String>> onlinePathsToResources;
	protected Map<Object, List<String>> offlinePathsToResources;

	public SISApplication() {
		super();
		pathsToResources = new HashMap<Object, List<String>>();
		pathsExcludedFromAuthenticator = new HashSet<String>();
		onlinePathsToResources = new HashMap<Object, List<String>>();
		offlinePathsToResources = new HashMap<Object, List<String>>();
		
		if (Debug.isDefaultInstance())
			Debug.setInstance(new SIS.SISDebugger());
	}

	/**
	 * CALLED WHEN ADDING PUBLIC ROUTER, ALL RESOURCES MUST BE ATTACHED BY THEN
	 */
	public abstract void init();

	@Override
	public Restlet getPrivateRouter() {
		return null;
	}

	public VFS getVFS() {
		// return app.getVFS();
		return SIS.get().getVFS();
	}

	@Override
	public Restlet getPublicRouter() {
		init();
		Router root = new Router(app.getContext());
		Router guarded = new Router(app.getContext());
		AuthnGuard guard = SIS.get().getGuard(app.getContext());

		Filter mainFilter = new Filter(app.getContext(), root) {

			@Override
			protected int beforeHandle(Request request, Response response) {
				SISPersistentManager.instance().getSession().beginTransaction();

				if (!SIS.get().isHostedMode() && SIS.amIOnline() && request.getProtocol().equals(Protocol.HTTP)) {
					Reference newRef = new Reference(request.getResourceRef());
					newRef.setHostPort(Integer.valueOf(443));
					newRef.setProtocol(Protocol.HTTPS);
					response.redirectPermanent(newRef);
					return STOP;
				} else
					return CONTINUE;
			}

			@Override
			protected void afterHandle(Request request, Response response) {
				// ADD NO CACHE DIRECTIVE
				RestletUtils.setHeader(response, "Cache-Control", "no-cache");
				RestletUtils.setHeader(response, MagicDisablingFilter.MAGIC_DISABLING_KEY, "true");
				
				final MediaType mt;
				if (response.isEntityAvailable())
					mt = null;
				else
					mt = response.getEntity().getMediaType();
				
				final Calendar cal = Calendar.getInstance();
				if (mt == null)
					cal.add(Calendar.DATE, -1);
				else if (MediaType.TEXT_ALL.includes(mt))
					cal.add(Calendar.HOUR, 24);
				else
					cal.add(Calendar.MONTH, 2);
				
				response.getEntity().setExpirationDate(cal.getTime());
				
				final String noHandle = RestletUtils.getHeader(response, NO_TRANSACTION_HANDLE);
				if (noHandle == null) {
					try {
						Transaction tsx = SISPersistentManager.instance().getSession().getTransaction();
						if (response.getStatus().isSuccess())
							tsx.commit();
						else
							tsx.rollback();
					} catch (HibernateException e) {
						Debug.println("Hibernate Error: {0}\n{1}", e.getMessage(), e);
						response.setStatus(Status.SERVER_ERROR_INTERNAL);
					}
				}				

				try {
					if (SIS.amIOnline()) {
						if (response.getStatus().isSuccess()) {
							if (response.isEntityAvailable() && SIS.get().isEncodeableEntity(response.getEntity()))
								response.setEntity(new EncodeRepresentation(Encoding.GZIP, response.getEntity()));
						}
					}
				} catch (Exception e) {
					// Not online!
				}
			}
		};

		mainFilter.setNext(root);
		root.attachDefault(guard);
		root.attach("/authn", guard);
		guard.setNext(guarded);

		for (Entry<Object, List<String>> entry : pathsToResources.entrySet()) {
			for (String path : entry.getValue()) {
				if (pathsExcludedFromAuthenticator.contains(path)) {
					Debug.println("adding {0} the path {1} to root", entry.getKey(), path);
					attachUniform(path, entry.getKey(), root);
				} else {
					Debug.println("adding {0} the path {1} to guared", entry.getKey(), path);
					attachUniform(path, entry.getKey(), guarded);
				}
			}
		}

		if (SIS.amIOnline()) {
			for (Entry<Object, List<String>> entry : onlinePathsToResources.entrySet()) {
				for (String path : entry.getValue()) {
					if (pathsExcludedFromAuthenticator.contains(path)) {
						Debug.println("adding {0} the path {1} to root", entry.getKey(), path);
						attachUniform(path, entry.getKey(), root);
					} else {
						Debug.println("adding {0} the path {1} to guarded", entry.getKey(), path);
						attachUniform(path, entry.getKey(), guarded);
					}
				}
			}
		} else {
			for (Entry<Object, List<String>> entry : offlinePathsToResources.entrySet()) {
				for (String path : entry.getValue()) {
					if (pathsExcludedFromAuthenticator.contains(path)) {
						attachUniform(path, entry.getKey(), root);
					} else {
						attachUniform(path, entry.getKey(), guarded);
					}
				}
			}
		}
		// httpsFilter.setNext(root);

		return mainFilter;

	}

	protected void attachUniform(String path, Object uniform, Router router) {
		if (uniform instanceof Restlet)
			attachRestlet(path, (Restlet) uniform, router);
		
		else
			attachResource(path, (Class<?>) uniform, router);
	}

	
	protected void attachResource(String path, Class<?> uniform, Router router) {
		router.attach(path, uniform);
	}

	protected void attachRestlet(String path, Restlet restlet, Router router) {
		router.attach(path, restlet);
	}

	/**
	 * Must be called during application initialization
	 * 
	 * Adds resources to the application's router
	 * 
	 * @param uniform
	 * @param paths
	 * @param allowOnline
	 * @param allowOffline
	 * @param bypassAuthentication
	 */
	public void addResource(Class<? extends Resource> uniform, List<String> paths, boolean allowOnline,
			boolean allowOffline, boolean bypassAuthentication) {
		if (allowOnline && allowOffline) {
			pathsToResources.put(uniform, paths);
		} else if (allowOnline) {
			onlinePathsToResources.put(uniform, paths);
		} else if (allowOffline) {
			offlinePathsToResources.put(uniform, paths);
		}
		if (bypassAuthentication)
			pathsExcludedFromAuthenticator.addAll(paths);

	}

	/**
	 * Must be called during application initialization
	 * 
	 * Adds resources to the application's router
	 * 
	 * @param uniform
	 * @param paths
	 * @param allowOnline
	 * @param allowOffline
	 * @param bypassAuthentication
	 */
	public void addResource(Class<? extends Resource> uniform, String path, boolean allowOnline, boolean allowOffline,
			boolean bypassAuthentication) {
		List<String> paths = new ArrayList<String>();
		paths.add(path);
		addResource(uniform, paths, allowOnline, allowOffline, bypassAuthentication);
	}

	public void addServerResource(Class<? extends ServerResource> uniform, String path, boolean allowOnline, boolean allowOffline, boolean bypassAuthentication) {
		List<String> paths = new ArrayList<String>();
		paths.add(path);
		addResource(uniform, paths, allowOnline, allowOffline, bypassAuthentication);
	}
	
	public void addServerResource(Class<? extends ServerResource> uniform, List<String> paths, boolean allowOnline, boolean allowOffline, boolean bypassAuthentication) {
		
		addResource(uniform, paths, allowOnline, allowOffline, bypassAuthentication);
	}

	/**
	 * Must be called during application initialization
	 * 
	 * Adds resources to the application's router
	 * 
	 * @param uniform
	 * @param paths
	 * @param allowOnline
	 * @param allowOffline
	 * @param bypassAuthentication
	 */
	public void addResource(Restlet uniform, List<String> paths, boolean allowOnline, boolean allowOffline,
			boolean bypassAuthentication) {
		if (allowOnline && allowOffline) {
			pathsToResources.put(uniform, paths);
		} else if (allowOnline) {
			onlinePathsToResources.put(uniform, paths);
		} else if (allowOffline) {
			offlinePathsToResources.put(uniform, paths);
		}
		if (bypassAuthentication) {
			Debug.println("Adding the path {0} to exclude from authenticator", paths);
			pathsExcludedFromAuthenticator.addAll(paths);
		}
	}

	/**
	 * Must be called during application initialization
	 * 
	 * Adds resources to the application's router
	 * 
	 * @param uniform
	 * @param paths
	 * @param allowOnline
	 * @param allowOffline
	 * @param bypassAuthentication
	 */
	public void addResource(Restlet uniform, String path, boolean allowOnline, boolean allowOffline,
			boolean bypassAuthentication) {
		List<String> paths = new ArrayList<String>();
		paths.add(path);
		addResource(uniform, paths, allowOnline, allowOffline, bypassAuthentication);
	}

	/**
	 * Must be called during application initialization
	 * 
	 * Adds resources to the application's router
	 * 
	 * @param uniform
	 * @param paths
	 * @param allowOnline
	 * @param allowOffline
	 * @param bypassAuthentication
	 */
	private void addResource(Object uniform, List<String> paths, boolean allowOnline, boolean allowOffline,
			boolean bypassAuthentication) {
		if (allowOnline && allowOffline) {
			pathsToResources.put(uniform, paths);
		} else if (allowOnline) {
			onlinePathsToResources.put(uniform, paths);
		} else if (allowOffline) {
			offlinePathsToResources.put(uniform, paths);
		}
		if (bypassAuthentication)
			pathsExcludedFromAuthenticator.addAll(paths);
	}

}
