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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.gogoego.api.authentication.AuthenticatorFactory;
import org.gogoego.api.authentication.CookieAuthenticatorFactory;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.VirtualHost;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.NodeCollection;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.events.VFSEvent;
import com.solertium.vfs.provider.FileVFS;
import com.solertium.vfs.utils.VFSUtils;

/**
 * Bootstrap uses the hosts.xml file, located in GOGOEGO_ROOT, to configure
 * GoGoEgoVirtualHost instances.
 * 
 * Its function is to supply the constructor arguments for this class. "host"
 * elements appear inside a "hosts" root element and define the following
 * attributes: "match", "id", and "httpsHost"
 * 
 * @see Bootstrap
 * @see ServerApplication
 * 
 * @author Rob Heittman <rob.heittman@solertium.com>
 * 
 */
public class GoGoEgoVirtualHost extends VirtualHost {

	private final boolean isProperlyConfigured;
	private final String siteID;
	private final Element hostNode;
	private final String vmroot;
	private final String canonicalHostDomain;

	private final HashMap<VFSPath, ApplicationHolder> pathToApplication;

	private ServerApplication application;

	/*private final VFSListener applicationListener = new VFSListener() {
		public void notifyEvent(VFSEvent vfsEvent) {
			updateClass(vfsEvent);
		}
	};*/

	/**
	 * Configures the virtual host with the ability to add other applications to
	 * the resource space as declared in the host node.
	 * 
	 * @param parentContext
	 *            the parent context
	 * @param isSslSupported
	 *            true if ssl is supported, false otherwise
	 * @param vmroot
	 *            the vmroot
	 * @param host
	 *            the host node containing information about this site,
	 *            including the resource domain, siteID, and application data
	 */
	public GoGoEgoVirtualHost(final Context parentContext, final boolean isSslSupported, final String vmroot,
			final Element host) {
		super(parentContext);
		final String match = host.getAttribute("match");
		setResourceDomain(match);

		this.pathToApplication = new HashMap<VFSPath, ApplicationHolder>();

		this.siteID = host.getAttribute("id");
		this.canonicalHostDomain = host.getAttribute("canonicalHostDomain");
		this.vmroot = vmroot;
		this.hostNode = host;
		this.isProperlyConfigured = match != null && configure(host, isSslSupported, vmroot);
	}

	public GoGoEgoVirtualHost(final Context parentContext, GoGoEgoVirtualHost other, final boolean isSslSupported) {
		this(parentContext, isSslSupported, other.vmroot, other.hostNode);
	}

	/**
	 * Determines if this virtual host was properly configured, meaning that it
	 * has a valid resource domain and all its required applications were
	 * started and attached properly.
	 * 
	 * @return true if so, false otherwise
	 */
	public boolean isProperlyConfigured() {
		return isProperlyConfigured;
	}

	/**
	 * Configures the basics of the virtual host, including the VFS for the
	 * default ServerApplication
	 * 
	 * @param host
	 *            the host node
	 * @param isSslSupported
	 *            true if ssl is supported, false otherwise
	 * @param vmroot
	 *            the vmroot for the vfs
	 * @return true if properly configured, false otherwise
	 */
	private boolean configure(final Element host, final boolean isSslSupported, final String vmroot) {
		final String match = host.getAttribute("match");
		final String id = host.getAttribute("id");
		final String httpsHost = host.getAttribute("httpsHost");
		final String username = host.getAttribute("username");
		final String password = host.getAttribute("password");
		final String repositoryURI = host.getAttribute("repositoryURI");

		GoGoDebug.system().println("Adding vhost {0} -> {1} with canonical host {2}", match, id, canonicalHostDomain);

		// Define the VFS
		final String vfsspec = host.getAttribute("vfs");
		File hostroot = new File(vmroot + "/" + id);
		File vfsroot = null;
		VFS vfs = null;
		if ((vfsspec == null) || vfsspec.trim().equalsIgnoreCase(""))
			vfsroot = new File(hostroot,"vfs");
		else
			vfsroot = new File(new File(vmroot), vfsspec);

		try {
			vfs = VFSFactory.getVFS(vfsroot);
		} catch (final NotFoundException nf) {
			try {
				vfs = FileVFS.create(vfsroot);
			} catch (final ConflictException unlikely) {
				GoGoDebug.system().println("VFS {0} could neither be opened nor created.", vfsroot.getPath());
				return false;
			}
		}

		//if (attachApplications(host, vfs)) {
			application = new ServerApplication(
				getContext(), id, new File(hostroot,"storage"), vfs, 
				httpsHost, username, password, repositoryURI, canonicalHostDomain
			);			
			this.attach(application);
			return true;
		/*} else
			return false;*/
	}

	/**
	 * Reload this application.  Should rid any OSGi nonsense that 
	 * can occur when an application is re-installed 
	 * @param registrationKey
	 */
	public void reloadApplication(String registrationKey) {
		this.application.reloadApplication(registrationKey);
	}

	/**
	 * Remove an application (and it's restlets)
	 * @param registrationKey
	 */
	public void removeApplication(String registrationKey) {
		this.application.removeApplication(registrationKey);
	}
	
	/**
	 * Reload this authenticator.  Should rid any OSGi nonsense that 
	 * can occur when an authenticator is re-installed 
	 * @param registrationKey
	 */
	public void reloadAuthenticator(String registrationKey, AuthenticatorFactory factory) {
		this.application.reloadAuthenticator(registrationKey, factory);
	}

	/**
	 * Remove an authenticator
	 * @param registrationKey
	 */
	public void removeAuthenticator(String registrationKey) {
		this.application.removeAuthenticator(registrationKey);
	}
	
	/**
	 * Reload this authenticator.  Should rid any OSGi nonsense that 
	 * can occur when an authenticator is re-installed 
	 * @param registrationKey
	 */
	public void reloadCookieAuthenticator(String registrationKey, CookieAuthenticatorFactory factory) {
		this.application.reloadCookieAuthenticator(registrationKey, factory);
	}

	/**
	 * Remove an authenticator
	 * @param registrationKey
	 */
	public void removeCookieAuthenticator(String registrationKey) {
		this.application.removeCookieAuthenticator(registrationKey);
	}

	class ClassLoaderFilter extends Filter {

		private ClassLoader current;
		private final ClassLoader alternate;

		public ClassLoaderFilter(Context context, ClassLoader alternate) {
			super(context);
			this.alternate = alternate;
		}

		protected int beforeHandle(Request request, Response response) {
			current = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(alternate);
			return Filter.CONTINUE;
		}

		protected void afterHandle(Request request, Response response) {
			Thread.currentThread().setContextClassLoader(current);
		}

	}

	/**
	 * Attempts to attach all the applications listed under the given host node.
	 * The "application" node can take the following attributes: - class
	 * (required) - designates the class to attempt to instantiate. This class
	 * MUST extend org.restlet.Application or it will not be accepted - attach
	 * (suggested) - this is the mount or attachment point for the given
	 * application, the "root" if you will. If no attach attribute is provided,
	 * a default one will be assigned at "/hostedApps/{class}" where "class" is
	 * the name of the class as specified. - required (optional) - if "true" is
	 * given as a value, this virtual host will not be attached to the Component
	 * if the given application, for whatever reason, can not be instantiated
	 * and attached. Otherwise, if this application fails to add, a default
	 * error message will be returned for all requests made to the attachment
	 * point. Defaults to false when this attribute is not provided.
	 * 
	 * @param host
	 *            the host node
	 * 
	 * @return true if all required applications were attached, false otherwise
	 */
	private boolean attachApplications(final Element host, final VFS vfs) {
		boolean isSuccess = true;
		final Iterator<Node> iterator = new NodeCollection(host.getElementsByTagName("application")).iterator();
		while (iterator.hasNext() && isSuccess) {
			final Node current = iterator.next();

			final boolean isRequired = DocumentUtils.impl.getAttribute(current, "required").equals("true");
			String attach = DocumentUtils.impl.getAttribute(current, "attach");
			if (attach.equals(""))
				attach = "/hostedApps/" + DocumentUtils.impl.getAttribute(current, "class");

			isSuccess = attachApplication(vfs, attach, isRequired, DocumentUtils.impl.getAttribute(current, "class"),
					DocumentUtils.impl.getAttribute(current, "jar"));
			GoGoDebug.system().println("Added Application {0} to {1}", 
					DocumentUtils.impl.getAttribute(current, "class"), host.getAttribute("id"));
		}

		return isSuccess;
	}

	private boolean attachApplication(final VFS vfs, final String attach, final boolean isRequired,
			final String className, final String jars) {
		boolean isSuccess = true;
		final ClassPackage cp;
		final Class<?> clazz;
		try {
			cp = createClass(className, jars, vfs, attach);
			clazz = cp.clazz;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			if (isRequired)
				isSuccess = false;
			else
				this.attach(attach, getStandardFailureApplication("as the application at this "
						+ "space could not be instantiated."));
			return isSuccess;
		}

		if (Application.class.isAssignableFrom(clazz)) {
			Constructor<?> c;
			try {
				c = clazz.getConstructor(new Class<?>[] { Context.class });
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				if (isRequired)
					isSuccess = false;
				else {
					this.attach(attach, getStandardFailureApplication("as the application at this "
							+ "space could not be instantiated."));
				}
				return isSuccess;
			}

			try {
				Filter f = new ClassLoaderFilter(getContext(), cp.loader);
				f.setNext((Application) c.newInstance(new Object[] { getContext() }));

				this.attach(attach, f);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				if (isRequired)
					isSuccess = false;
				else {
					this.attach(attach, getStandardFailureApplication("as the application at this "
							+ "space could not be started."));
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
				if (isRequired)
					isSuccess = false;
				else {
					this.attach(attach, getStandardFailureApplication("as the application at this "
							+ "space could not be started."));
				}
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				if (isRequired)
					isSuccess = false;
				else {
					this.attach(attach, getStandardFailureApplication("as the application at this "
							+ "space could not be started."));
				}
			}
		} else {
			GoGoDebug.system().println("Only Restlet Applications can be attached.");
			if (isRequired)
				isSuccess = false;
			else
				this.attach(attach, getStandardFailureApplication("as the application at this "
						+ "space could not be started."));
		}

		return isSuccess;
	}

	private ClassPackage createClass(final String className, final String jars, final VFS vfs, final String attach)
			throws ClassNotFoundException {
		final ArrayList<VFSPath> jarList = new ArrayList<VFSPath>();
		if (!"".equals(jars)) {
			final ApplicationHolder holder = new ApplicationHolder();
			holder.attach = attach;
			holder.className = className;
			holder.classPath = jars;
			holder.vfs = vfs;

			final String[] split = jars.split(";");
			for (String current : split) {
				final VFSPath path;
				try {
					path = VFSUtils.parseVFSPath(current);
				} catch (VFSUtils.VFSPathParseException e) {
					GoGoDebug.system().println("Invalid path specified: {0}", current);
					continue;
				}
				if (vfs.exists(path)) {
					jarList.add(path);
					//vfs.addListener(path, applicationListener);
					pathToApplication.put(path, holder);
				} else {
					GoGoDebug.system().println("Could not find specified path {0} on VFS", path);
					continue;
				}
			}
		}
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (!jarList.isEmpty())
			loader = vfs.createClassLoader(jarList, loader);

		ClassPackage c = new ClassPackage();
		c.clazz = Class.forName(className, false, loader);
		c.loader = loader;

		return c;
	}

	private static class ClassPackage {
		private Class<?> clazz;
		private ClassLoader loader;
	}

	public void updateClass(VFSEvent event) {
		final ApplicationHolder holder = pathToApplication.get(event.getURIs()[0]);
		if (holder != null) {
			Restlet restlet = null;
			for (Route r : getRoutes()) {
				if (r.getTemplate().getPattern().equals(holder.attach)) {
					restlet = r.getNext();
					break;
				}
			}
			if (restlet instanceof ClassLoaderFilter) {
				detach(restlet);
				try {
					restlet.stop();
				} catch (Exception e) {
					GoGoDebug.system().println("Error occurred stopping application: {0}", e.getMessage());
				}
				if (attachApplication(holder.vfs, holder.attach, true, holder.className, holder.classPath))
					GoGoDebug.system().println("Successfully updated application");
				else
					GoGoDebug.system().println("Found application, but failed to update");
			} else
				GoGoDebug.system().println("Could not find appropriate application");
		}
	}

	private Restlet getStandardFailureApplication(final String reply) {
		return new Restlet() {
			public void handle(Request request, Response response) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(new StringRepresentation("Unable to resolve " + request.getResourceRef().getPath()
						+ ", " + reply));
			}
		};
	}

	public String getSiteID() {
		return siteID;
	}

	public Element getHostNode() {
		return hostNode;
	}

	public String getVmroot() {
		return vmroot;
	}

	private static class ApplicationHolder {
		private String classPath;
		private String className;
		private String attach;
		private VFS vfs;
	}

}
