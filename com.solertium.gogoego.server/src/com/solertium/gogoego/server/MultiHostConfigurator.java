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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Context;
import org.restlet.routing.VirtualHost;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.solertium.gogoego.server.lib.manager.container.ManagerApplication;
import com.solertium.gogoego.server.lib.manager.container.ManagerVirtualHost;
import com.solertium.util.restlet.CookieUtility;

/**
 * MultiHostConfigurator.java
 * 
 * Configures multiple hosts for a given GoGoEgo container.
 * 
 * @author rob.heittman
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class MultiHostConfigurator {

	public void configure(final Bootstrap bootstrap, final Context context, final String vmroot) {
		final ManagerApplication.ManagerProperties managerProps = ManagerApplication.getManagerSiteID();
		boolean provideExternalManager = managerProps != null;

		final ArrayList<String> successfulAdds = configure(bootstrap.isSslSupported(), bootstrap.getHosts(), context,
				vmroot);

		final String internalManagerKey = CookieUtility.newUniqueID().toLowerCase();
		
		bootstrap.getInternalRouter().attach("/" + internalManagerKey,
				new ManagerApplication(context, internalManagerKey, vmroot, successfulAdds, bootstrap, true));

		// Add the external manager, if nec'y
		if (provideExternalManager && successfulAdds.add(managerProps.getSiteID())){
			List<VirtualHost> newHostList = new ArrayList<VirtualHost>();
			newHostList.add(new ManagerVirtualHost(context, managerProps.getMatch(), managerProps.getSiteID(), vmroot,
				successfulAdds, bootstrap, false));
			newHostList.addAll(bootstrap.getHosts());
			bootstrap.setHosts(new CopyOnWriteArrayList<VirtualHost>(newHostList));
			try{
				bootstrap.updateHosts();
			} catch (Exception logged) {
				logged.printStackTrace();
			}
		}
	}

	private ArrayList<String> configure(final boolean isSslSupported, final List<VirtualHost> virtualHosts,
			final Context context, final String vmroot) {
		final ArrayList<String> successfulAdds = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			public boolean add(String e) {
				return !contains(e) && super.add(e);
			}
		};
		GoGoDebug.system().println("Attempting to build from {0}/hosts.xml", vmroot);
		final File file_fs = new File(vmroot + "/hosts.xml");
		try {
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					new FileInputStream(file_fs));
			final NodeList hosts = doc.getDocumentElement().getElementsByTagName("host");
			for (int i = 0; i < hosts.getLength(); i++) {
				final Element host = (Element) hosts.item(i);
				final String id = host.getAttribute("id");

				final GoGoEgoVirtualHost vh = new GoGoEgoVirtualHost(context, isSslSupported, vmroot, host);
				if (vh.isProperlyConfigured()) {
					virtualHosts.add(vh);
					successfulAdds.add(id);
					GoGoDebug.system().println("Added {0}", id);
				} else
					GoGoDebug.system().println("## Could Not add {0}", id);

			}
		} catch (final ParserConfigurationException pcx) {
			GoGoDebug.system().println(
					"Java XML Parser configuration problems; GoGoEgo will not work.");
		} catch (final SAXException sx) {
			GoGoDebug.system().println(
					"Could not read {0}.\n" + "No hosts defined.\n"
							+ "Set GOGOEGO_VMROOT appropriately " + "and/or create and/or repair hosts.xml", file_fs.getPath());
		} catch (final IOException io) {
			GoGoDebug.system().println(
					"Could not read {0}.\n" + "No hosts defined.\n"
							+ "Set GOGOEGO_VMROOT appropriately " + "and/or create and/or repair hosts.xml", file_fs.getPath());
		}
		return successfulAdds;
	}

}
