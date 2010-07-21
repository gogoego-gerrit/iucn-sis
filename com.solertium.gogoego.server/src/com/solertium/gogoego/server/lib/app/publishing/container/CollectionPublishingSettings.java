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
package com.solertium.gogoego.server.lib.app.publishing.container;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ImporterSettings.java
 * 
 * Fetch and provide importer settings. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionPublishingSettings {
	
	private static final VFSPath PATH = new VFSPath("/(SYSTEM)/publishing/config.xml");
	
	private final Map<String, String> settings;
	private final VFS vfs;
	
	public CollectionPublishingSettings(VFS vfs) {
		this.vfs = vfs;
		settings = new LinkedHashMap<String, String>();
	}
	
	public void init() throws GoGoEgoApplicationException {
		Document document;
		if (!vfs.exists(PATH)) {
			final Document init = BaseDocumentUtils.impl.newDocument();
			final Element root = init.createElement("root");
			
			final Element field = init.createElement("field");
			field.setAttribute("name", "key");
			field.setTextContent("changeme");
			
			final Element target = init.createElement("field");
			target.setAttribute("name", "target");
			target.setTextContent("target-site.gogoego.com");
			
			root.appendChild(field);
			root.appendChild(target);
			
			init.appendChild(root);
			
			if (save(init))
				document = init;
			else
				throw new GoGoEgoApplicationException("Could not write init file.");
		}
		else {
			try {
				document = vfs.getDocument(PATH);
			} catch (IOException e) {
				throw new GoGoEgoApplicationException(PATH + " reported as existing but could not be loaded", e);
			}
		}
		
		loadDocument(document, false);
	}
	
	public void load(Document document) {
		loadDocument(document, true);
	}
	
	private void loadDocument(Document document, boolean save) {
		final NodeCollection fields = new NodeCollection(
			document.getDocumentElement().getChildNodes()	
		);
		
		for (Node field : fields)
			if (field.getNodeType() == Node.ELEMENT_NODE)
				settings.put(DocumentUtils.impl.getAttribute(field, "name"), field.getTextContent());
		
		if (save)
			save(document);		
	}
	
	private boolean save(Document document) {
		return DocumentUtils.writeVFSFile(PATH.toString(), vfs, document);
	}
	
	public String getSetting(String key) {
		return getSetting(key, null);
	}
	
	public String getSetting(String key, String defaultValue) {
		return settings.get(key) == null ? defaultValue : settings.get(key); 
	}
	
	public String toXML() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<root>");
		for (Map.Entry<String, String> entry : settings.entrySet()) {
			builder.append("<field name=\"" + entry.getKey() + "\">");
			builder.append("<![CDATA[" + entry.getValue() + "]]>");
			builder.append("</field>");
		}
		builder.append("</root>");
		return builder.toString();
	}

}
