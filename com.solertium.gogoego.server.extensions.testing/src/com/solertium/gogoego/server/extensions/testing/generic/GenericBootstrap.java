package com.solertium.gogoego.server.extensions.testing.generic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.gogoego.api.applications.ApplicationEventsAPI;
import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.applications.TemplateRegistryAPI;
import org.gogoego.api.authentication.AuthenticatorFactory;
import org.gogoego.api.caching.CacheHandler;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.images.ImageManipulatorFactory;
import org.gogoego.api.images.ImageManipulatorHelper;
import org.gogoego.api.images.ImageManipulatorPreferences;
import org.gogoego.api.plugins.GoGoBackboneAPI;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.scripting.ScriptableObjectFactory;
import org.gogoego.api.utils.BestMatchPluginBroker;
import org.gogoego.api.utils.PluginBroker;
import org.gogoego.api.utils.ProductProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.engine.Engine;
import org.restlet.routing.Router;

import com.solertium.util.CurrentBinary;
import com.solertium.util.SysDebugger;
import com.solertium.util.restlet.ScratchResourceBin;
import com.solertium.util.restlet.StandardServerComponent;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;

/**
 * GenericBootstrap.java
 * 
 * The generic bootstrap will allow you to test your GoGoEgo Application 
 * outside the context of GoGoEgo, with a few provisions.
 * 
 * It creates a custom implementation of the ServerApplicationAPI, needed 
 * to initialize your application.  It will provide you with a valid 
 * org.restlet.Context object, but the rest of the values will likely 
 * be null or invalid.  You may override this with your own custom 
 * functionality if needed, but this is meant to be generic in nature.
 * 
 * The initialization and resource mounting aspects directly mimic how 
 * GoGoEgo will install your Application on startup, so success or 
 * failure in this aspect will likely carry over when your application 
 * is deployed.
 * 
 * The only function you are required to override is 
 * <code>getGoGoEgoApplication()</code>.  It should return a new instance 
 * of your application.  There is also a protected 
 * <code>getRegistrationKey()</code> function, which is used as the 
 * mount point for your resources.  It defaults to "testing", but you 
 * may want to replace it with your true bundle name.  This is probably 
 * not necessary in most cases.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class GenericBootstrap extends StandardServerComponent {
	
	String publicRouteMount, privateRouteMount;
	
	public GenericBootstrap() {
		this(11001, 11002);
	}
	
	public GenericBootstrap(int httpPort, int httpsPort) {
		super(httpPort, httpsPort);
		this.publicRouteMount = "/apps";
		this.privateRouteMount = "/admin/apps";
	}
	
	protected void createServers() {
		Engine.setInstance(new JettyEngine());
		super.createServers();
	}
	
	@Override
	protected void setupDefaultVirtualHost() {
		final Context context = getContext().createChildContext();
		/*getDefaultHost().attach(new Application(context) {
			public Restlet createRoot() {
				return buildRouter(context);
			}
		});*/
		getDefaultHost().attach(buildRouter(context));
	}
	
	private Router buildRouter(Context context) {
		File file = CurrentBinary.getDirectory(this);
		if (file.getAbsolutePath().endsWith(File.separatorChar + "bin"))
			file = file.getParentFile();
		ProductProperties.impl.setWorkingDirectory(file);
		
		final ServerApplicationAPI api = getServerApplicationAPI(context);
		
		final TestingBackbone backbone = new TestingBackbone(api);
		
		GoGoEgo.build(backbone);
		
		return initAndInstall(context, backbone, api);
	}
	
	Router initAndInstall(Context context, TestingBackbone backbone, ServerApplicationAPI api) {
		final GoGoEgoApplication app = getGoGoEgoApplication();
		backbone.setApplication(getRegistrationKey(), app);
		
		app.init(api, getRegistrationKey());
		if (app.isInstalled()) {
			final Router router = new Router(context);
			router.attach(buildMount(publicRouteMount, app.getRegistrationKey()), app.getPublicRouter());
			router.attach(buildMount(privateRouteMount, app.getRegistrationKey()), app.getPrivateRouter());
			return router;
		}
		else
			throw new RuntimeException("Could not install your application.");
	}
	
	/**
	 * Allows you to set the mount of the public router 
	 * to something other than /apps.  This is useful 
	 * in some testing contexts, but is not supported 
	 * in GoGoEgo at this time, so if you choose to use 
	 * this method, ensure your services will continue 
	 * to work behind /apps
	 * @param mount
	 */
	protected void setPublicRouterMountPoint(String mount) {
		if (mount != null)
			this.publicRouteMount = formatCustomMount(mount);
	}
	
	/**
	 * Allows you to set the mount of the private router 
	 * to something other than /admin/apps.  This is 
	 * useful in some testing contexts, but is not 
	 * supported in GoGoEgo at this time, so if you 
	 * choose to use this method, ensure your services 
	 * will continue to work behind /admin/apps
	 * @param mount
	 */
	protected void setPrivateRouterMountPoint(String mount) {
		if (mount != null)
			this.privateRouteMount = formatCustomMount(mount);
	}
	
	/**
	 * Here's where you come in -- return your GGE application.
	 * @return
	 */
	protected abstract GoGoEgoApplication getGoGoEgoApplication();
	
	/**
	 * Override if you need to return the real deal
	 * @return
	 */
	protected String getRegistrationKey() {
		return "testing";
	}
	
	protected VFS getVFS() {
		return null;
	}
	
	protected ServerApplicationAPI getServerApplicationAPI(Context context) {
		return new TestingServerApplication(context, getVFS());
	}
	
	private String formatCustomMount(String mount) {
		String result = mount;
		if (result.endsWith("/"))
			result = result.substring(0, result.length()-1);
		return result;
	}
	
	String buildMount(String mount, String registrationKey) {
		String result = "";
		if (!mount.equals(""))
			result += mount;
		if (!registrationKey.equals(""))
			result += "/" + registrationKey;
		return result;
	}
	
	public static class TestingBackbone implements GoGoBackboneAPI {
		
		private final ServerApplicationAPI api;
		private final TestDebuggingImpl debug;
		private final EmptyBundleContext context;
		
		private final Map<String, GoGoEgoApplication> applications;
		
		public TestingBackbone(ServerApplicationAPI api)  {
			this.api = api;
			this.debug = new TestDebuggingImpl();
			this.context = new EmptyBundleContext();
			this.applications = new HashMap<String, GoGoEgoApplication>();
		}
			
		public PluginBroker<ScriptableObjectFactory> getScriptableObjectBroker() {
			throw new UnsupportedOperationException();
		}
		
		public ImageManipulatorHelper getImageManipulatorHelper(Context context) {
			return new ImageManipulatorHelper() {
				public String getResizedURI(String path, int size, String mode) {
					// TODO Auto-generated method stub
					return null;
				}
				
				public String getResizedURI(String path, int size) {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
		
		public BestMatchPluginBroker<ImageManipulatorFactory, ImageManipulatorPreferences> getImageManipulatorBroker() {
			throw new UnsupportedOperationException();
		}
		
		public PluginBroker<GoGoEgoApplicationFactory> getGoGoEgoApplicationBroker() {
			throw new UnsupportedOperationException();
		}
		
		public ServerApplicationAPI getFromContext(Context context) {
			return api;
		}
		
		public GoGoDebugger getDebugger(String name) {
			return debug;
		}
		
		public File getCurrentFileStorage(String key) {
			File file;
			try {
				file = File.createTempFile("testing", "tmp");
			} catch (IOException e) {
				throw new UnsupportedOperationException();
			}
			
			File f = new File(file.getParentFile(), key);
			if (!f.exists())
				f.mkdirs();
			
			return f;
		}
		
		public PluginBroker<ClassLoader> getClassLoaderBroker() {
			throw new UnsupportedOperationException();
		}
		
		public BundleContext getBundleContext() {
			return context;
		}
		
		public PluginBroker<AuthenticatorFactory> getAuthenticatorBroker() {
			throw new UnsupportedOperationException();
		}
		
		public GoGoEgoApplication getApplication(Context context, String registrationKey) {
			return applications.get(registrationKey);
		}
		
		public void setApplication(String registrationKey, GoGoEgoApplication application) {
			applications.put(registrationKey, application);
		}
		
		public CacheHandler getCacheHandler() {
			throw new UnsupportedOperationException();
		}
		
		public GoGoEgoBaseRepresentation applyTemplating(GoGoEgoBaseRepresentation templateRepresentation,
				GoGoEgoBaseRepresentation baseRepresentation) {
			throw new UnsupportedOperationException();
		}
		
		public GoGoEgoBaseRepresentation applyTemplating(TemplateDataAPI template,
				GoGoEgoBaseRepresentation baseRepresentation) {
			throw new UnsupportedOperationException();
		}
		
		public GoGoEgoBaseRepresentation applyTemplating(String templateKey, GoGoEgoBaseRepresentation baseRepresentation)
				throws NotFoundException {
			throw new UnsupportedOperationException();
		}
		
		public void addCookie(CookieSetting cookieSetting) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static class TestDebuggingImpl extends SysDebugger implements GoGoDebugger {

		public TestDebuggingImpl() {
			super();
		}

		public TestDebuggingImpl(int logLevel, OutputStream output) {
			super(logLevel, output);
		}

		public TestDebuggingImpl(int logLevel) {
			super(logLevel);
		}

	}
	
	public static class TestingServerApplication implements ServerApplicationAPI {
		
		private final Context context;
		private final ScratchResourceBin bin;
		private final VFS vfs;
		
		public TestingServerApplication(Context context, VFS vfs) {
			this.context = context;
			this.vfs = vfs;
			this.bin = new ScratchResourceBin();
		}
		
		public ApplicationEventsAPI getApplicationEvents() {
			return null;
		}
		
		public Context getContext() {
			return context;
		}
		
		public String getHttpsHost() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public ScratchResourceBin getScratchResourceBin() {
			return bin;
		}
		
		public String getSiteID() {
			return "testsite";
		}
		
		public TemplateRegistryAPI getTemplateRegistry() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public VFS getVFS() {
			return vfs;
		}
		
		public boolean isHostedMode() {
			// TODO Auto-generated method stub
			return true;
		}
	}
	
	@SuppressWarnings("unchecked")
	static class EmptyBundleContext implements BundleContext {

		public void addBundleListener(BundleListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void addFrameworkListener(FrameworkListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void addServiceListener(ServiceListener listener, String filter)
				throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			
		}

		public void addServiceListener(ServiceListener listener) {
			// TODO Auto-generated method stub
			
		}

		public Filter createFilter(String filter) throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			return null;
		}

		public ServiceReference[] getAllServiceReferences(String clazz,
				String filter) throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			return new ServiceReference[0];
		}

		public Bundle getBundle() {
			// TODO Auto-generated method stub
			return null;
		}

		public Bundle getBundle(long id) {
			// TODO Auto-generated method stub
			return null;
		}

		public Bundle[] getBundles() {
			// TODO Auto-generated method stub
			return new Bundle[0];
		}

		public File getDataFile(String filename) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getProperty(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getService(ServiceReference reference) {
			// TODO Auto-generated method stub
			return null;
		}

		public ServiceReference getServiceReference(String clazz) {
			// TODO Auto-generated method stub
			return null;
		}

		public ServiceReference[] getServiceReferences(String clazz,
				String filter) throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			return new ServiceReference[0];
		}

		public Bundle installBundle(String location, InputStream input)
				throws BundleException {
			// TODO Auto-generated method stub
			return null;
		}

		public Bundle installBundle(String location) throws BundleException {
			// TODO Auto-generated method stub
			return null;
		}

		public ServiceRegistration registerService(String clazz,
				Object service, Dictionary properties) {
			// TODO Auto-generated method stub
			return null;
		}

		public ServiceRegistration registerService(String[] clazzes,
				Object service, Dictionary properties) {
			// TODO Auto-generated method stub
			return null;
		}

		public void removeBundleListener(BundleListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void removeFrameworkListener(FrameworkListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void removeServiceListener(ServiceListener listener) {
			// TODO Auto-generated method stub
			
		}

		public boolean ungetService(ServiceReference reference) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}

}
