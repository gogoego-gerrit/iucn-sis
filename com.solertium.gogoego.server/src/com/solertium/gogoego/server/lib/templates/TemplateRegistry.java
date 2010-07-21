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

package com.solertium.gogoego.server.lib.templates;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.applications.TemplateRegistryAPI;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.util.BaseTagListener;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.TagFilter.Tag;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

/**
 * TemplateRegistry.java
 * 
 * Registry for templates.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class TemplateRegistry implements TemplateRegistryAPI {

	public static final VFSPath REGISTRY_PATH = new VFSPath("/(SYSTEM)/registry/templates.xml");

	private final ArrayList<String> paths;
	private final HashMap<String, TemplateData> registry;
	private final VFS vfs;

	public static TemplateRegistry getFromContext(Context context) {
		return ServerApplication.getFromContext(context).getTemplateRegistry();
	}

	public TemplateRegistry(final VFS vfs, final ArrayList<String> paths) {
		this.vfs = vfs;

		this.paths = new ArrayList<String>();
		this.paths.addAll(paths);

		registry = new HashMap<String, TemplateData>();

		refresh();
	}

	public TemplateRegistry(final VFS vfs, final String path) {
		this.vfs = vfs;

		paths = new ArrayList<String>();
		paths.add(path);

		registry = new HashMap<String, TemplateData>();

		refresh();
	}
	
	protected GoGoDebugger log() {
		return GoGoDebug.get("debug");
	}

	public void addFileToRegistry(final String currentPath) {
		if (currentPath.endsWith(".html") || currentPath.endsWith(".htm"))
			if (currentPath.indexOf("/") == -1)
				try {
					final TemplateData templateData = new TemplateData(vfs
							.getReader(VFSUtils.parseVFSPath(currentPath)), currentPath);
					templateData.setUri("/" + currentPath);
					templateData.setDisplayName(currentPath);

					// Add it twice so that this template can be matched by its
					// name OR its URI
					registry.put("/" + currentPath, templateData);
					registry.put(currentPath, templateData);
				} catch (final Exception e) {
					TrivialExceptionHandler.ignore(this, e);
				}
			else {
				final String[] split = currentPath.split("/");
				final String relativeName = split[split.length - 1];
				try {

					final TemplateData templateData = new TemplateData(vfs
							.getReader(VFSUtils.parseVFSPath(currentPath)), currentPath);
					templateData.setUri("/" + currentPath);
					templateData.setDisplayName(relativeName);

					// Add it twice so that this template can be matched by its
					// name OR its URI
					registry.put(relativeName, templateData);
					registry.put(currentPath, templateData);
				} catch (final NotFoundException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
	}

	private String formatPath(String key) {
		if (key.endsWith("/"))
			key = key.substring(0, key.length() - 1);
		return key;
	}

	public Document getDocument() {
		Document document;
		try {
			document = DocumentUtils.getReadWriteDocument(REGISTRY_PATH, vfs);
		} catch (IOException e) {
			document = null;
		}
		if (document == null)
			document = DocumentUtils.impl.createDocumentFromString("<registry></registry>");
		return document;
	}
	
	public TemplateDataAPI getRegisteredTemplate(final String key) {
		return registry.get(formatPath(key));
	}

	public Collection<TemplateDataAPI> getTemplateListing() {
		final ArrayList<TemplateDataAPI> listing = new ArrayList<TemplateDataAPI>();
		final Iterator<TemplateData> iterator = registry.values().iterator();
		while (iterator.hasNext()) {
			final TemplateData current = iterator.next();
			if (!listing.contains(current))
				listing.add(current);
		}
		return listing;
	}

	public boolean isRegistered(final String key) {
		return key != null && registry.containsKey(formatPath(key));
	}

	public void refresh() {
		registry.clear();
		final Map<String, Map<String, String>> properties = getProperties();
		for (int i = 0; i < paths.size(); i++) {
			final VFSPath currentPath;
			try {
				currentPath = VFSUtils.parseVFSPath(paths.get(i));
			} catch (Exception e) {
				continue;
			}

			log().println("Template Registry checking path: {0}", currentPath);
			if (!vfs.exists(currentPath)) {
				continue;
			} else
				try {
					traverseDirectory(currentPath, properties);
					log().println("- Added path: {0}", currentPath);
				} catch (final Exception e) {
					TrivialExceptionHandler.ignore(this, e);
				}
		}

		save();
	}

	private void traverseDirectory(VFSPath directory, Map<String, Map<String, String>> properties) throws IOException {
		if (!vfs.isCollection(directory))
			return;

		final VFSPathToken[] files = vfs.list(directory);
		for (VFSPathToken file : files) {
			final VFSPath uri = directory.child(file);
			if (vfs.exists(uri)) {
				if (vfs.isCollection(uri))
					traverseDirectory(uri, properties);
				else 
					registerTemplate(uri, properties.get(uri.toString()));
			}
		}
	}

	private void registerTemplate(VFSPath uri, Map<String, String> properties) {
		if (uri.toString().endsWith(".html") || uri.toString().endsWith(".htm")) {
			try {
				final TemplateData templateData = new TemplateData(vfs.getReader(uri), uri.toString());
				if (properties != null) {
					String displayName = properties.get("name");
					if (displayName == null || "".equals(displayName))
						displayName = uri.getName();
					templateData.setDisplayName(displayName);
					
					String contentType = properties.get("contentType");
					if (contentType == null || "".equals(contentType))
						contentType = "text/html";
					templateData.setContentType(contentType);
				}
				else {
					templateData.setDisplayName(uri.getName());
					templateData.setContentType("text/html");
				}

				// Add it twice so that this template can be matched by its
				// name OR its URI
				registry.put(uri.toString(), templateData);
				registry.put(templateData.getDisplayName(), templateData);
			} catch (final Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}

	public boolean removeFileFromRegistry(final String currentPath) {
		final Iterator<String> iterator = registry.keySet().iterator();
		final ArrayList<String> keysToRemove = new ArrayList<String>();

		while (iterator.hasNext()) {
			final String key = iterator.next();
			final TemplateData current = registry.get(key);
			try {
				if (current.getUri().equalsIgnoreCase(currentPath)
						|| current.getDisplayName().equalsIgnoreCase(currentPath))
					keysToRemove.add(key);
			} catch (NullPointerException impossible) {
				TrivialExceptionHandler.impossible(this, impossible);
				continue;
			}
		}

		for (int i = 0; i < keysToRemove.size(); i++)
			registry.remove(keysToRemove.get(i));

		return !keysToRemove.isEmpty();
	}

	public void save() {
		final Document document = DocumentUtils.impl.createDocumentFromString("<registry></registry>");
		// Iterator<String> iterator = registry.keySet().iterator();
		final Iterator<TemplateDataAPI> iterator = getTemplateListing().iterator();
		while (iterator.hasNext()) {
			final TemplateDataAPI data = iterator.next();

			final Element newChild = document.createElement("template");
			newChild.setAttribute("name", data.getDisplayName());
			newChild.setAttribute("uri", data.getUri());
			newChild.setAttribute("contentType", data.getContentType());

			document.getFirstChild().appendChild(newChild);
		}

		log().println("Template Registry Saved: {0}", DocumentUtils.writeVFSFile(REGISTRY_PATH.toString(), vfs, document));

		log().println("{0}", getTemplateListing());
	}
	
	private Map<String, Map<String, String>> getProperties() {
		final Map<String, Map<String, String>> map = 
			new HashMap<String, Map<String,String>>();
		
		if (vfs.exists(REGISTRY_PATH)) {
			final Reader reader;
			try {
				reader = vfs.getReader(REGISTRY_PATH);
			} catch (IOException e) {
				e.printStackTrace();
				return map;
			}
				
			final TagFilter tf = new TagFilter(reader);
			tf.shortCircuitClosingTags = false;
			tf.registerListener(new BaseTagListener() {
				public List<String> interestingTagNames() {
					final ArrayList<String> list = new ArrayList<String>();
					list.add("template");
					return list;
				}
				public void process(Tag t) throws IOException {
					final Map<String, String> properties = new HashMap<String, String>();
					properties.put("name", t.getAttribute("name"));
					properties.put("contentType", t.getAttribute("contentType"));
					
					map.put(t.getAttribute("uri"), properties);
				}
			});
			try {
				tf.parse();
			} catch (IOException e) {
				e.printStackTrace();
				try {
					reader.close();
				} catch (IOException f) {
					TrivialExceptionHandler.ignore(this, f);
				}
			}
		}
				
		return map;
	}

}
