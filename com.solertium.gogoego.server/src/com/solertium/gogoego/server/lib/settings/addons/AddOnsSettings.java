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
package com.solertium.gogoego.server.lib.settings.addons;

import java.util.Map;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.GoGoEgoApplicationMetaData;
import org.gogoego.api.applications.HasSettingsUI;
import org.gogoego.api.applications.IsPrivate;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Application;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorker;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.VFS;

/**
 * AddOnsSettings.java
 * 
 * Handles fetching INIT information for Add-Ons. Since add-ons can come from
 * anywhere, this class can not handle fetching and saving for AUTHORITY or DATA
 * protocols.
 * 
 * @author carl.scott
 * 
 */
public class AddOnsSettings extends SimpleSettingsWorker {

	public AddOnsSettings(VFS vfs) {
		super(vfs);
	}

	/**
	 * There will never be an authority file for add ons?
	 */
	public Representation getAuthority(String key) throws SimpleSettingsWorkerException {
		throw new SimpleSettingsWorkerException(key + " not available for add ons");
	}

	/**
	 * Data getting is handled elsewhere
	 */
	public Representation getData(String key) throws SimpleSettingsWorkerException {
		throw new SimpleSettingsWorkerException(key + " not available for add ons");
	}

	public Representation getInit() throws SimpleSettingsWorkerException {
		final ServerApplication sa = ServerApplication.
			getFromContext(Application.getCurrent().getContext());
		
		final Document initDoc = DocumentUtils.impl.newDocument();
		initDoc.appendChild(initDoc.createElement("root"));
		
		final Map<String, Map<String, String>> metadata = 
			PluginAgent.getGoGoEgoApplicationBroker().getMetadata();

		for (Map.Entry<String, GoGoEgoApplicationFactory> entry : PluginAgent.getGoGoEgoApplicationBroker().getPlugins()
				.entrySet()) {
			GoGoEgoApplicationMetaData md;
			try {
				md = entry.getValue().getMetaData();
			} catch (Throwable e) {
				GoGoDebug.system().println("Big OSGi error");
				e.printStackTrace();
				continue;
			}
			
			final Element element = initDoc.createElement("application");
			element.setAttribute("class", entry.getKey());
			if (md != null) {
				element.setAttribute("name", md.getName());
				element.appendChild(BaseDocumentUtils.impl.
					createCDATAElementWithText(initDoc, "description", 
						md.getDescription()
					)
				);
			}
			else {
				String className = entry.getKey();
				if (className.indexOf('.') != -1) 
					className = className.substring(className.lastIndexOf('.'));
				element.setAttribute("name", className);
				element.appendChild(BaseDocumentUtils.impl.
					createCDATAElementWithText(initDoc, "description", 
						"No META-DATA available."
					)
				);
			}
			
			GoGoEgoApplication current;
			try {
				current = entry.getValue().newInstance();
				current.init(GoGoEgo.get().getFromContext(sa.getContext()), entry.getKey());
			} catch (Throwable e) {
				continue;
			}
			
			/*
			 * If application is not already installed, check if it 
			 * can be installed.  If not, skip it.
			 */
			
			if (current instanceof IsPrivate && 
				!sa.isApplicationInstalled(entry.getKey()) && 
				!((IsPrivate)current).isSiteAllowed(sa.getContext(), sa.getSiteID())) {
				continue;
			}
			
			if (current instanceof HasSettingsUI) {
				element.setAttribute("uri", ((HasSettingsUI) current).getSettingsURL());
			}
			else
				element.setAttribute("uri", "local");
			
			final Map<String, String> curMetadata = metadata.get(entry.getKey());
			if (curMetadata != null)
				for (Map.Entry<String, String> m : curMetadata.entrySet())
					element.setAttribute(m.getKey(), m.getValue());
			
			initDoc.getDocumentElement().appendChild(element);
		}

		return new DomRepresentation(MediaType.TEXT_XML, initDoc);
	}

	/**
	 * Data setting is handled elsewhere
	 */
	public void setData(String key, Document document) throws SimpleSettingsWorkerException {
		throw new SimpleSettingsWorkerException(key + " not available for add ons");
	}

}
